package pl.edu.pw.elka.adoptimizer.http

import java.util.UUID.randomUUID

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import pl.edu.pw.elka.adoptimizer.adinsertion.AdInserterActor
import pl.edu.pw.elka.adoptimizer.api.{ AdApiActor, SystemApiActor }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes.BayesianTextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.classifier.linear.LogisticClassifier
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.Stopwords
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.{ SimpleStemmedTokenizer, SimpleTokenizer, StemmedUnigramTokenizer }
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer
import pl.edu.pw.elka.adoptimizer.categorization.{ EnsembleActor, EnsemblePart, GenericClassifierActor }
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

  implicit val system: ActorSystem = ActorSystem("AdOptimizer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val vectorizer = new TfIdfVectorizer(minCount = 100, maxCount = 1000, tokenizer = new StemmedUnigramTokenizer(Stopwords.en))
  val lrActor = system.actorOf(Props(new GenericClassifierActor(new LogisticClassifier(vectorizer), "lr")), "lrActor")

  val bayesActor = system.actorOf(Props(new GenericClassifierActor(BayesianTextClassifier(new SimpleStemmedTokenizer), "bayes")), "bayesActor")
  //ConfigFactory.load().getConfig("ensemble.classifiers")

  val ensembleActor = system.actorOf(Props(new EnsembleActor(EnsemblePart(lrActor), EnsemblePart(bayesActor))), "ensemble")

  val parsingActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(WebsiteParserActor.props), "parsersPool")

  val insertionActorsPool: ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_INSERTER_ACTORS).
      props(Props(new AdInserterActor(parsingActorsPool, ensembleActor))), "insertersPool")

  val adApiActor: ActorRef = system.actorOf(Props(new AdApiActor(insertionActorsPool)), "adApiActor")
  val systemApiActor: ActorRef = system.actorOf(Props(new SystemApiActor(ensembleActor)), "systemApiActor")

  val adApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(AdRoutes.routes(adApiActor), "localhost", 8080)
  val systemApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(SystemRoutes.routes(systemApiActor), "localhost", 8090)

  println(s"Ad server online at port 8080. System server online at port 8090.\nPress RETURN to stop...")

  StdIn.readLine()

  unbind(adApiBindingFuture)(() => unbind(systemApiBindingFuture)(() => system.terminate()))
}