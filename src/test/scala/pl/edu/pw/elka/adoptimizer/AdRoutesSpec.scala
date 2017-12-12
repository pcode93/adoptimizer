package pl.edu.pw.elka.adoptimizer

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import pl.edu.pw.elka.adoptimizer.api.AdApiActor
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.JsonSupport
import pl.edu.pw.elka.adoptimizer.http.routes.{ AdForWebsite, AdRoutes }

class AdRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonSupport {
  val testAdCategory = "test"
  val testAdContent = "test content"
  val testWebsiteContent = "<html>test</html>"

  val adActor: ActorRef =
    system.actorOf(AdApiActor.props, "adActor")

  lazy val routes = AdRoutes.routes(adActor)

  "AdRoutes" should {
    "return website for (GET /ad)" in {
      val adRequest = AdForWebsite(testWebsiteContent, Ad(testAdCategory, testAdContent))
      val adEntity = Marshal(adRequest).to[MessageEntity].futureValue
      val request = Get("/ad").withEntity(adEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        entityAs[String] should ===(testWebsiteContent)
      }
    }
  }
}
