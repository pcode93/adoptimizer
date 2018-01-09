package pl.edu.pw.elka.adoptimizer.http.routes

import akka.actor.{ Actor, Props }
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import pl.edu.pw.elka.adoptimizer.api.AdApiActor.InsertAd
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.JsonSupport

import scala.concurrent.duration._

class AdRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest with JsonSupport {

  final case class AdApiActorMock() extends Actor {
    override def receive: Receive = {
      case InsertAd(html, ad) =>
        if (html == testWebsite && ad == Ad(testAdCategory, testAdContent)) sender() ! expectedWebsite
    }
  }

  val testAdCategory = "test"
  val testAdContent = "test content"
  val testWebsite = "test website"
  val expectedWebsite = "expected website"

  implicit val timeout = Timeout(5 seconds)
  lazy val routes = AdRoutes.routes(system.actorOf(Props(AdApiActorMock())))

  "AdRoutes" should {
    "return modified website for (POST /ad)" in {
      val adRequest = AdForWebsite(testWebsite, Ad(testAdCategory, testAdContent))
      val adEntity = Marshal(adRequest).to[MessageEntity].futureValue
      val request = Post("/ad").withEntity(adEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        contentType should ===(ContentTypes.`text/plain(UTF-8)`)

        entityAs[String] should ===(expectedWebsite)
      }
    }
  }
}
