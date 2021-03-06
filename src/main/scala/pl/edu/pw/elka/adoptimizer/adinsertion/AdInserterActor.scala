package pl.edu.pw.elka.adoptimizer.adinsertion

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.categorization.EnsembleClassifierException
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Classify
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor.{ AppendContentToParagraph, ExtractParagraphs, Paragraph }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

object AdInserterActor {
  type ClassificationResultsType = Map[Double, String]

  final case class ParsedParagraphs(paragraphs: List[Paragraph])
  final case class HtmlWithAd(htmlWithAd: String)
  final case class ClassificationResults(bestParagraph: String)

  final case class AdInserterException(message: String, cause: Throwable) extends RuntimeException(message, cause)

  def props: Props = Props[AdInserterActor]
}

class AdInserterActor(parsingActors: ActorRef, classificationEnsemble: ActorRef) extends Actor with ActorLogging {
  import AdInserterActor._
  import pl.edu.pw.elka.adoptimizer.api.AdApiActor.InsertAd

  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = context.dispatcher

  var requestOriginActor: ActorRef = _
  var adToInsert: Ad = _
  var targetWebsite: String = _

  def receive: Receive = {
    case InsertAd(website, ad) =>
      requestOriginActor = sender()
      adToInsert = ad
      targetWebsite = website
      parsingActors ! ExtractParagraphs(website)
    case ParsedParagraphs(paragraphs) =>
      if (paragraphs.isEmpty) {
        requestOriginActor ! targetWebsite
      } else {
        classifyParagraphs(paragraphs.map((x: String) => Sample(x, adToInsert.category)): List[Sample])
      }
    case ClassificationResults(bestParagraph) =>
      parsingActors ! AppendContentToParagraph(targetWebsite, bestParagraph, adToInsert.content)
    case HtmlWithAd(htmlWithAd) =>
      requestOriginActor ! htmlWithAd
  }

  def classifyParagraphs(samples: List[Sample]): Unit = {
    val scores = samples.map(sample => (classificationEnsemble ? Classify(sample))
      .mapTo[Double].map((sample.content, _)))

    Future.sequence(scores).onComplete {
      case Success(results) =>
        self ! ClassificationResults(selectBestParagraph(results))
      case Failure(exception) =>
        throw AdInserterException("Exception getting scores for paragraphs", exception)
    }
  }

  def selectBestParagraph(scores: List[(String, Double)]): String = {
    println(scores)
    scores.maxBy(_._2)._1
  }
}
