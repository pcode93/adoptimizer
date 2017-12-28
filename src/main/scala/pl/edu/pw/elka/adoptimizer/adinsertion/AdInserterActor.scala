package pl.edu.pw.elka.adoptimizer.adinsertion

import java.util.concurrent.TimeUnit

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.util.Timeout
import akka.pattern.ask
import pl.edu.pw.elka.adoptimizer.categorization.EnsembleActor
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Classify
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.AdOptimizer
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor.{ AppendContentToParagraph, ExtractParagraphs, Paragraph }

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.concurrent.Await

object AdInserterActor {
  type ClassificationResultsType = mutable.SortedMap[Double, Paragraph]
  final case class ParsedParagraphs(paragraphs: List[Paragraph])
  final case class HtmlWithAd(htmlWithAd: String)
  final case class ClassificationResults(classificationResults: ClassificationResultsType)
  def props: Props = Props[AdInserterActor]
}

class AdInserterActor extends Actor with ActorLogging {
  import AdInserterActor._
  import pl.edu.pw.elka.adoptimizer.api.AdApiActor.InsertAd

  var requestOriginActor: ActorRef = _
  var adToInsert: Ad = _
  var targetWebsite: String = _

  private var parsingActor = context.actorOf(WebsiteParserActor.props)
  private var classificationActor = context.actorOf(WebsiteParserActor.props)

  def receive: Receive = {
    case InsertAd(website, ad) =>
      requestOriginActor = sender()
      adToInsert = ad
      targetWebsite = website
      parsingActor ! ExtractParagraphs(website)
    case ParsedParagraphs(paragraphs) =>
      if (paragraphs.isEmpty) {
        requestOriginActor ! targetWebsite
      } else {
        classifyParagraphs(paragraphs)
      }
    case ClassificationResults(classificationResults) =>
      val (bestResult, bestParagraph) = classificationResults.head
      parsingActor ! AppendContentToParagraph(targetWebsite, bestParagraph, adToInsert.content)
    case HtmlWithAd(htmlWithAd) =>
      requestOriginActor ! htmlWithAd
      context.stop(parsingActor)
      context.stop(classificationActor)
      context.stop(self)
  }

  def classifyParagraphs(paragraphs: List[Paragraph]) = {
    val classificationResults: ClassificationResultsType = mutable.SortedMap()
    classificationResults += ((1.0, paragraphs.head))
    self ! ClassificationResults(classificationResults)
  }

}
