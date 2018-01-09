package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
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

  private var accuracy: Double = 0D
  context.system.eventStream.subscribe(self, classOf[NewModel])

  def classify(sample: Sample, sender: ActorRef): Unit = {
    val res = classifier.classify(sample.content).getOrElse(sample.category, 0D)
    log.info(s"$uuid score: $res")
    sender ! res
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot) => {
      val state = snapshot.asInstanceOf[(Double, Any)]

      accuracy = state._1
      classifier.load(state._2)
    }
  }

  override def persistenceId: String = uuid

  override def receiveCommand: Receive = {
    case Classify(sample) => classify(sample, sender())
    case NewModel(acc, state, id) => if (id == uuid && acc > accuracy) {
      classifier.load(state)

      accuracy = acc
      saveSnapshot((accuracy, classifier.save()))
    }
  }
}
