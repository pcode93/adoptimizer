package pl.edu.pw.elka.adoptimizer.workers

import akka.actor.{Actor, ActorLogging, Props}
import akka.pattern.ask
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.workers.ParserActor.ParseSite
import pl.edu.pw.elka.adoptimizer.workers._


object WorkerActor {
  final case class InsertAdWork( website: String, ad: Ad)

  def props: Props = Props[WorkerActor]

}

class WorkerActor extends Actor  with ActorLogging {
  import WorkerActor._

  def receive: Receive = {
    case InsertAdWork(website,ad) =>
      sender()! DoWork(website,ad)

  }

  def DoWork(website: String, ad: Ad): String = {
    val parserActor = context.actorOf(ParserActor.props, "parserActor")
    val parsingResult = parserActor ? ParseSite(website)

  }
}
