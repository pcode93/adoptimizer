package pl.edu.pw.elka.adoptimizer.api

import java.nio.charset.CodingErrorAction

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import pl.edu.pw.elka.adoptimizer.categorization.GenericClassifierActor
import pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic.LogisticClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Train
import pl.edu.pw.elka.adoptimizer.categorization.util.CsvParser

import scala.io.{ Codec, Source }

object SystemApiActor {
  final case class TrainEnsemble(datasetUri: String)

  def props: Props = Props[SystemApiActor]
}

class SystemApiActor extends Actor with ActorLogging {
  import SystemApiActor._

  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  val actor: ActorRef =
    context.actorOf(Props(new GenericClassifierActor(new LogisticClassifier(), "lr")), "lrActor")

  def receive: Receive = {
    case TrainEnsemble(uri) =>
      actor ! Train(CsvParser.parse(Source.fromFile(uri).getLines().toList, ";"))
  }
}
