package pl.edu.pw.elka.adoptimizer.api

import akka.actor.{ Actor, ActorLogging, Props }

object SystemApiActor {
  final case class TrainEnsemble()

  def props: Props = Props[SystemApiActor]
}

class SystemApiActor extends Actor with ActorLogging {
  import SystemApiActor._

  def receive: Receive = {
    case TrainEnsemble() =>
      sender() ! null
  }
}
