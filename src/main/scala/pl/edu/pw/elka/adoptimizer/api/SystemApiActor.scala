package pl.edu.pw.elka.adoptimizer.api

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Train
import pl.edu.pw.elka.adoptimizer.categorization.util.CsvParser

import scala.io.Source

object SystemApiActor {
  final case class TrainEnsemble(datasetUri: String)

  def props: Props = Props[SystemApiActor]
}

class SystemApiActor extends Actor with ActorLogging {
  import SystemApiActor._
  val actor: ActorRef = null

  def receive: Receive = {
    case TrainEnsemble(uri) =>
      actor ! Train(CsvParser.parse(Source.fromFile(uri).getLines().toList))
  }
}
