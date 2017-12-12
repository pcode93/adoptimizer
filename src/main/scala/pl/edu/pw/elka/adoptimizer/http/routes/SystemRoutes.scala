package pl.edu.pw.elka.adoptimizer.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.api.AdApiActor.InsertAd
import pl.edu.pw.elka.adoptimizer.http.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

object SystemRoutes extends JsonSupport {
  implicit val timeout = Timeout(5.seconds)
  def routes(systemActor: ActorRef): Route =
    pathPrefix("ad") {
      pathEnd {
        get {
          entity(as[AdForWebsite]) { adForWebsite =>
            val websiteWithAd: Future[String] =
              (systemActor ? InsertAd(adForWebsite.websiteHtml, adForWebsite.ad)).mapTo[String]
            complete(websiteWithAd)
          }
        }
      }
    }
}
