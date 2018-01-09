package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ActorLogging, ActorRef, Props}
import akka.persistence.{PersistentActor, SnapshotOffer}
import pl.edu.pw.elka.adoptimizer.categorization.ClassifierActor.NewModel
import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Classify
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

object ClassifierActor {
  final case class NewModel(accuracy: Double, state: Any, uuid: String)
  def props: Props = Props[ClassifierActor]
}

class ClassifierActor(classifier: TextClassifier, uuid: String)
    extends PersistentActor with ActorLogging {

  def classify(sample: Sample, sender: ActorRef): Unit = {
    val res = classifier.classify(sample.content).getOrElse(sample.category, 0D)
    log.info(s"$uuid score: $res")
    sender ! res
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot) => classifier.load(snapshot)
  }

  override def persistenceId: String = uuid

  override def receiveCommand: Receive = {
    case Classify(sample) => classify(sample, sender())
    case NewModel(acc, state, id) => if(id == uuid) {
      classifier.load(state)
      saveSnapshot(classifier.save())
    }
  }
}
