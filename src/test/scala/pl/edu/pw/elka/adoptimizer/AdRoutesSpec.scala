package pl.edu.pw.elka.adoptimizer

import akka.actor.ActorRef
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import pl.edu.pw.elka.adoptimizer.api.AdApiActor
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.{ AdOptimizer, JsonSupport }
import pl.edu.pw.elka.adoptimizer.http.routes.{ AdForWebsite, AdRoutes }

class AdRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonSupport {
  val testAdCategory = "test"
  val testAdContent = "test content"
  val testWebsiteContentNoParagraphs = "<html>test</html>"
  val testWebsiteContentWithParagraphs = "<html><head></head><p>test1</p><p>test2</p><p>test3</p></html>"

  val adActor: ActorRef =
    system.actorOf(AdApiActor.props, "adActor")

  lazy val routes = AdRoutes.routes(adActor)

  "AdRoutes" should {
    "return unmodified website for (POST /ad), website with no <p> tags" in {
      val adRequest = AdForWebsite(testWebsiteContentNoParagraphs, Ad(testAdCategory, testAdContent))
      val adEntity = Marshal(adRequest).to[MessageEntity].futureValue
      val request = Post("/ad").withEntity(adEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        entityAs[String] should ===(testWebsiteContentNoParagraphs)
      }
    }
  }
  "AdRoutes" should {
    "return website with inserted ad at the end of the first <p> for (POST /ad), website with <p> tags" in {
      val adRequest = AdForWebsite(testWebsiteContentWithParagraphs, Ad(testAdCategory, testAdContent))
      val adEntity = Marshal(adRequest).to[MessageEntity].futureValue
      val request = Post("/ad").withEntity(adEntity)
      val expectedWebsiteWithInsertedAd = "<html>\n <head></head>\n <body>\n  <p>test1<b style=\"display: block\">test content</b></p>\n  <p>test2</p>\n  <p>test3</p>\n </body>\n</html>"

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        entityAs[String] should ===(expectedWebsiteWithInsertedAd)
      }
    }
  }
}
