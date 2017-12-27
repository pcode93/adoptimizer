package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ ActorLogging, ActorRef }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.{ Classify, Train }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class GenericClassifierActor(classifier: TextClassifier, uuid: String)
    extends PersistentActor with ActorLogging {

  def classify(sample: Sample, sender: ActorRef): Unit =
    sender ! classifier.classify(sample)

  def fit(samples: List[Sample]): Unit = {

    classifier.fit(samples)
    //saveSnapshot(classifier.save())
  }

  override def receiveRecover: Receive = {
    case SnapshotOffer(_, snapshot) => classifier.load(snapshot)
  }

  override def persistenceId: String = uuid

  override def receiveCommand: Receive = {
    case Classify(sample) => classify(sample, sender())
    case Train(samples) => fit(samples)
  }
}
