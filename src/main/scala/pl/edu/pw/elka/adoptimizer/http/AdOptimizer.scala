package pl.edu.pw.elka.adoptimizer.http

import java.util.UUID.randomUUID

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import pl.edu.pw.elka.adoptimizer.adinsertion.AdInserterActor
import pl.edu.pw.elka.adoptimizer.api.{ AdApiActor, SystemApiActor }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes.BayesianTextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.SimpleStemmedTokenizer
import pl.edu.pw.elka.adoptimizer.categorization.{ EnsembleActor, EnsemblePart, GenericClassifierActor }
import pl.edu.pw.elka.adoptimizer.http.routes.{ AdRoutes, SystemRoutes }
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

final case class AdOptimizer()

object AdOptimizer extends App {

  private final val NR_OF_INSERTER_ACTORS = 10;
  private final val NR_OF_PARSING_ACTORS = 10;

  lazy val log = Logging(system, classOf[AdOptimizer])

  private def unbind(server: Future[ServerBinding])(oncomplete: () => Unit) =
    server.flatMap(_.unbind()).onComplete({ done =>
      done.failed.map { ex => log.error(ex, "Failed unbinding.") }
      oncomplete()
    })

  implicit val system: ActorSystem = ActorSystem("AdOptimizer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val newsGroups = Array(
    "alt.atheism",
    "comp.graphics",
    "comp.os.ms-windows.misc",
    "comp.sys.ibm.pc.hardware",
    "comp.sys.mac.hardware",
    "comp.windows.x",
    "misc.forsale",
    "rec.autos",
    "rec.motorcycles",
    "rec.sport.baseball",
    "rec.sport.hockey",
    "sci.crypt",
    "sci.electronics",
    "sci.med",
    "sci.space",
    "soc.religion.christian",
    "talk.politics.guns",
    "talk.politics.mideast",
    "talk.politics.misc",
    "talk.religion.misc"
  )

  val classificationActors: Array[ActorRef] = Array.fill(newsGroups.length)(system.
    actorOf(Props(new GenericClassifierActor(
      new BayesianTextClassifier(new SimpleStemmedTokenizer),
      randomUUID().toString
    ))))

  val ensembleParts = new Array[EnsemblePart](newsGroups.length)

  for (i <- 0 to newsGroups.length)
    ensembleParts(i) = new EnsemblePart(List(newsGroups(i)), classificationActors(i), 1D)

  val ensembleActor = system.actorOf(Props(new EnsembleActor(ensembleParts: _*)))

  val insertionActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_INSERTER_ACTORS).props(AdInserterActor.props), "insertersPool")

  val parsingActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(WebsiteParserActor.props), "parsersPool")

  val adApiActor: ActorRef = system.actorOf(Props(new AdApiActor(insertionActorsPool)), "adApiActor")
  val systemApiActor: ActorRef = system.actorOf(SystemApiActor.props, "systemApiActor")

  val adApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(AdRoutes.routes(adApiActor), "localhost", 8080)
  val systemApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(SystemRoutes.routes(systemApiActor), "localhost", 8090)

  println(s"Ad server online at port 8080. System server online at port 8090.\nPress RETURN to stop...")

  StdIn.readLine()

  unbind(adApiBindingFuture)(() => unbind(systemApiBindingFuture)(() => system.terminate()))
}