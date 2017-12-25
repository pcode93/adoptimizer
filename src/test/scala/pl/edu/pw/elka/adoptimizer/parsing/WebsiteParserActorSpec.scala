package pl.edu.pw.elka.adoptimizer.parsing

import akka.actor.{ ActorRef, ActorSystem }
import akka.testkit.TestKit
import akka.util.Timeout
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import org.scalatest.concurrent.ScalaFutures
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor.{ AppendContentToParagraph, ExtractParagraphs }
import akka.pattern.ask
import org.scalatest.time.{ Millis, Seconds, Span }

import scala.concurrent.duration._

class WebsiteParserActorSpec extends TestKit(ActorSystem("ParserSpec"))
    with WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)
  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(500, Millis))

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val html = "<html><head></head><body><p>Paragraph 1</p><img><p>Paragraph 2</p></body></html>"
  val parserActor: ActorRef = system.actorOf(WebsiteParserActor.props, "parserActor")

  "Website parser" should {
    "return all paragraphs for website" in {
      whenReady(parserActor ? ExtractParagraphs(html)) { result =>
        result shouldBe List("Paragraph 1", "Paragraph 2")
      }
    }

    "return website with content appended to correct paragraph" in {
      val expectedHtml = "<html><head></head><body><p>Paragraph 1" +
        "<b style=\"display: block\">test</b></p><img><p>Paragraph 2</p></body></html>"
      whenReady(parserActor ? AppendContentToParagraph(html, "Paragraph 1", "test")) { result =>
        result.toString.split("\n").map(_.trim).reduce(_ + _) shouldBe expectedHtml
      }
    }
  }
}
