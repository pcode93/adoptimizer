package pl.edu.pw.elka.adoptimizer.http

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import pl.edu.pw.elka.adoptimizer.adinsertion.AdInserterActor
import pl.edu.pw.elka.adoptimizer.api.{ AdApiActor, SystemApiActor }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.{ ClassifierActor, EnsembleActor, EnsemblePart, ModelBuildingActor }
import pl.edu.pw.elka.adoptimizer.http.routes.{ AdRoutes, SystemRoutes }
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor

import scala.concurrent.{ ExecutionContext, Future }
import scala.io.StdIn

final case class AdOptimizer()

object AdOptimizer extends App {

  private final val NR_OF_INSERTER_ACTORS = 10
  private final val NR_OF_PARSING_ACTORS = 10

  lazy val log = Logging(system, classOf[AdOptimizer])

  private def unbind(server: Future[ServerBinding])(oncomplete: () => Unit): Unit =
    server.flatMap(_.unbind()).onComplete({ done =>
      done.failed.map { ex => log.error(ex, "Failed unbinding.") }
      oncomplete()
    })

  private def getClassifier(ofType: String, uuid: String, name: String, textClassifier: TextClassifier) =
    if (ofType == "classifying") system.actorOf(Props(new ClassifierActor(textClassifier, uuid)), name)
    else system.actorOf(Props(new ModelBuildingActor(textClassifier, uuid)), name)

  private def getEnsemble(ofType: String): ActorRef = {
    val lrActor = getClassifier(ofType, "lr", s"$ofType-lrActor", TextClassifier.of("lr"))
    val bayesActor = getClassifier(ofType, "bayes", s"$ofType-bayesActor", TextClassifier.of("bayes"))

    system.actorOf(Props(new EnsembleActor(EnsemblePart(lrActor), EnsemblePart(bayesActor))), s"$ofType-ensemble")
  }

  implicit val system: ActorSystem = ActorSystem("AdOptimizer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val classifyingEnsemble = getEnsemble("classifying")
  val trainableEnsemble = getEnsemble("trainable")

  val parsingActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(WebsiteParserActor.props), "parsersPool")

  val insertionActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_INSERTER_ACTORS).
      props(Props(new AdInserterActor(parsingActorsPool, classifyingEnsemble))), "insertersPool")

  val adApiActor: ActorRef = system.actorOf(Props(new AdApiActor(insertionActorsPool)), "adApiActor")
  val systemApiActor: ActorRef = system.actorOf(Props(new SystemApiActor(trainableEnsemble)), "systemApiActor")

  val adApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(AdRoutes.routes(adApiActor), "localhost", 8080)
  val systemApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(SystemRoutes.routes(systemApiActor), "localhost", 8090)

  println(s"Ad server online at port 8080. System server online at port 8090.\nPress RETURN to stop...")

  StdIn.readLine()

  unbind(adApiBindingFuture)(() => unbind(systemApiBindingFuture)(() => system.terminate()))
}