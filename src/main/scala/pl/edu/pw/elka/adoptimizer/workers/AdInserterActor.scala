package pl.edu.pw.elka.adoptimizer.workers

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import pl.edu.pw.elka.adoptimizer.categorization.Sample
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.AdOptimizer
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor
import pl.edu.pw.elka.adoptimizer.parsing.WebsiteParserActor.ExtractParagraphs



object AdInserterActor {
  final case class InsertAdWork( website: String, ad: Ad)

  def props: Props = Props[AdInserterActor]

}

class AdInserterActor extends Actor  with ActorLogging {
  import AdInserterActor._

  def receive: Receive = {
    case InsertAdWork(website,ad) =>
      sender()! ParseAndStartClassification(website,ad)

  }

  def ParseAndStartClassification(website: String, ad: Ad): String = {
    val paragraphs = AdOptimizer.parsingActorsPool ? ExtractParagraphs(website)
    for( p <- paragraphs){
      AdOptimizer.classificationActorsPool ! ClassifyParagraph(new Sample(p, ad.category))
    }
  }
}
