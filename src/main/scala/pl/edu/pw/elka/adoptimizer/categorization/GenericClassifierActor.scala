package pl.edu.pw.elka.adoptimizer.categorization

import java.util.Date

import akka.actor.{ ActorLogging, ActorRef, Props }
import akka.persistence.{ PersistentActor, SnapshotOffer }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.{ Classify, Train }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

object GenericClassifierActor {
  def props: Props = Props[GenericClassifierActor]
}

class GenericClassifierActor(classifier: TextClassifier, uuid: String)
    extends PersistentActor with ActorLogging {

  def classify(sample: Sample, sender: ActorRef): Unit =
    sender ! classifier.classify(sample)

  def fit(samples: List[Sample]): Unit = {
    log.info(s"Classifier $uuid training started at ${new Date()}")

    /*
    val sets = samples
      .groupBy(_.category)
      .map(category => category._2.splitAt((category._2.length * 0.8).toInt))
      .reduce((x,y) => (x._1 ++ y._1, x._2 ++ y._2))
      */

    classifier.fit(samples)
    log.info(s"Classifier $uuid training finished at ${new Date()}")

    saveSnapshot(classifier.save())
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
