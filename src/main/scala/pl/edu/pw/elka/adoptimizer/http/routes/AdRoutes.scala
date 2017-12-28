package pl.edu.pw.elka.adoptimizer.http.routes

import akka.actor.ActorRef
import akka.http.scaladsl.model.{ HttpEntity, StatusCodes }
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.http.scaladsl.model.ContentTypes.`text/html(UTF-8)`
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.api.AdApiActor.InsertAd
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.JsonSupport

import scala.concurrent.Future
import scala.concurrent.duration._

final case class AdForWebsite(websiteHtml: String, ad: Ad)

object AdRoutes extends JsonSupport {
  implicit val timeout = Timeout(5.seconds)
  def routes(adActor: ActorRef): Route =
    pathPrefix("ad") {
      pathEnd {
        post {
          entity(as[AdForWebsite]) { adForWebsite =>
            val websiteWithAd: Future[String] =
              (adActor ? InsertAd(adForWebsite.websiteHtml, adForWebsite.ad)).mapTo[String]
            complete(websiteWithAd)
          }
        }
      }
    }
}
