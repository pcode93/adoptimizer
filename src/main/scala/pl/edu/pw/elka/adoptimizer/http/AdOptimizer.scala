package pl.edu.pw.elka.adoptimizer.http

import akka.actor.{ActorRef, ActorSystem}
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.routing.RoundRobinPool
import akka.stream.ActorMaterializer
import pl.edu.pw.elka.adoptimizer.api.{AdApiActor, SystemApiActor}
import pl.edu.pw.elka.adoptimizer.categorization.EnsembleActor
import pl.edu.pw.elka.adoptimizer.http.routes.{AdRoutes, SystemRoutes}
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor
import pl.edu.pw.elka.adoptimizer.workers.AdInserterActor

import scala.concurrent.{ExecutionContext, Future}
import scala.io.StdIn

final case class AdOptimizer()

object AdOptimizer extends App {
  private final val NR_OF_PARSING_ACTORS = 5;
  private final val NR_OF_INSERTION_ACTORS = 5;
  private final val NR_OF_CLASSIFICATION_ACTORS = 5;

  lazy val log = Logging(system, classOf[AdOptimizer])

  private def unbind(server: Future[ServerBinding])(oncomplete: () => Unit) =
    server.flatMap(_.unbind()).onComplete({ done =>
      done.failed.map { ex => log.error(ex, "Failed unbinding.") }
      oncomplete()
    })

  implicit val system: ActorSystem = ActorSystem("AdOptimizer")
  implicit val materializer: ActorMaterializer = ActorMaterializer()
  implicit val executionContext: ExecutionContext = system.dispatcher

  val adApiActor: ActorRef = system.actorOf(AdApiActor.props, "adApiActor")
  val systemApiActor: ActorRef = system.actorOf(SystemApiActor.props, "systemApiActor")

  val adApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(AdRoutes.routes(adApiActor), "localhost", 8080)
  val systemApiBindingFuture: Future[ServerBinding] =
    Http().bindAndHandle(SystemRoutes.routes(systemApiActor), "localhost", 8090)


  val parsingActorsPool : ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(WebsiteParserActor.props), "parsersPool")

  val insertionActorsPool : ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(AdInserterActor.props), "insertersPool")

  val classificationActorsPool : ActorRef =
    system.actorOf(RoundRobinPool(NR_OF_PARSING_ACTORS).props(EnsembleActor.props), "classifi")


  println(s"Ad server online at port 8080. System server online at port 8090.\nPress RETURN to stop...")

  StdIn.readLine()

  unbind(adApiBindingFuture)(() => unbind(systemApiBindingFuture)(() => system.terminate()))
}