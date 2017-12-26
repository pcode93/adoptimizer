package pl.edu.pw.elka.adoptimizer.parsing

import scala.collection.JavaConverters._
import akka.actor.{ Actor, Props }
import org.jsoup.Jsoup
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor.{ AppendContentToParagraph, ExtractParagraphs, Paragraph }

object WebsiteParserActor {
  final case class ExtractParagraphs(html: String)
  final case class AppendContentToParagraph(html: String, paragraph: String, content: String)
  def props: Props = Props[WebsiteParserActor]

  type Paragraph = String
}

class WebsiteParserActor extends Actor {

  private def getParagraphs(html: String): List[Paragraph] =
    Jsoup.parse(html).select("p").asScala.map(_.text()).toList

  private def insertAfterParagraph(html: String, paragraphToInsertAfter: String, contentToInsert: String) = {
    val doc = Jsoup.parse(html)

    doc.select("p").forEach(paragraph =>
      if (paragraph.text() == paragraphToInsertAfter) paragraph
        .appendElement("b")
        .attr("style", "display: block")
        .appendText(contentToInsert))

    doc.html()
  }

  override def receive: Receive = {
    case ExtractParagraphs(html) => sender() ! getParagraphs(html)
    case AppendContentToParagraph(html, paragraph, content) => sender() ! insertAfterParagraph(html, paragraph, content)
  }
}
