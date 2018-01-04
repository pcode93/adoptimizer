package pl.edu.pw.elka.adoptimizer.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.api.SystemApiActor.TrainEnsemble
import pl.edu.pw.elka.adoptimizer.http.JsonSupport

import scala.concurrent.duration._

object SystemRoutes extends JsonSupport {
  implicit val timeout = Timeout(5.seconds)
  def routes(systemActor: ActorRef): Route =
    pathPrefix("system") {
      path("train") {
        post {
          entity(as[String]) { datasetUri =>
            systemActor ! TrainEnsemble(datasetUri)
            complete(StatusCodes.OK)
          }
        }
      }
    }
}
