package pl.edu.pw.elka.adoptimizer.api

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Train
import pl.edu.pw.elka.adoptimizer.categorization.util.{ CsvParser, FileReader }

object SystemApiActor {
  final case class TrainEnsemble(datasetUri: String)

  def props: Props = Props[SystemApiActor]
}

class SystemApiActor(classificationEnsemble: ActorRef) extends Actor with ActorLogging {
  import SystemApiActor._

  def receive: Receive = {
    case TrainEnsemble(uri) =>
      classificationEnsemble ! Train(CsvParser.parse(FileReader.fromPath(uri), ";"))
  }
}
