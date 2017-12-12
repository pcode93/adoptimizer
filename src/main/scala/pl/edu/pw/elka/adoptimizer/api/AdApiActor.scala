package pl.edu.pw.elka.adoptimizer.api

import akka.actor.{ Actor, ActorLogging, Props }
import pl.edu.pw.elka.adoptimizer.domain.Ad

object AdApiActor {
  final case class InsertAd(website: String, ad: Ad)

  def props: Props = Props[AdApiActor]
}

class AdApiActor extends Actor with ActorLogging {
  import AdApiActor._

  def receive: Receive = {
    case InsertAd(website, ad) =>
      sender() ! website
  }
}
