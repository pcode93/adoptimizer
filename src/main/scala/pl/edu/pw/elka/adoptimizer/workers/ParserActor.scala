package pl.edu.pw.elka.adoptimizer.workers



import akka.actor.{Actor, ActorLogging, Props}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import pl.edu.pw.elka.adoptimizer.domain.DataTypes.TagAndText
import pl.edu.pw.elka.adoptimizer.domain.ParsedSite

import scala.collection.mutable.ListBuffer

object ParserActor {
  final case class ParseSite( website: String)

  def props: Props = Props[WorkerActor]
}

class ParserActor extends Actor with ActorLogging {
  import ParserActor._

  def receive: Receive = {
    case ParseSite( website : String) =>
      sender() ! DoParsing(website)
  }

  def DoParsing(website: String): String = {
    val parsingResult : ParsedSite
    val tagsAndTexts : ListBuffer[TagAndText] = ListBuffer()
    val doc : Document = Jsoup.parse(website)
    // TODO Parsing: f.e. add unique identifiers to all <p> in document
    // and put extracted paragraphs to tuples with their tags
    tagsAndTexts += "TestTag" -> "Test Text"

    parsingResult.taggedWebsite = doc.toString()
    parsingResult.tagsAndText = tagsAndTexts.toList

    parsingResult
  }
}
