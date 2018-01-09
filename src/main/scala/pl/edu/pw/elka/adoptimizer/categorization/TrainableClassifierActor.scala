package pl.edu.pw.elka.adoptimizer.categorization
import java.util.Date

import akka.actor.{Actor, ActorLogging}
import pl.edu.pw.elka.adoptimizer.categorization.ClassifierActor.NewModel
import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Train
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class TrainableClassifierActor(classifier: TextClassifier, uuid: String)
  extends Actor with ActorLogging with Trainable {

  override def fit(samples: List[Sample]): Unit = {
    log.info(s"Classifier $uuid training started at ${new Date()}")

    val sets = samples
      .groupBy(_.category)
      .map(category => category._2.splitAt((category._2.length * 0.5).toInt))
      .reduce((x, y) => (x._1 ++ y._1, x._2 ++ y._2))

    classifier.fit(sets._1)
    log.info(s"Classifier $uuid training finished at ${new Date()}")

    val acc = sets._2
      .map(sample => if (classifier.classify(sample.content).maxBy(_._2)._1 == sample.category) 1 else 0)
      .sum.toDouble / sets._2.length.toDouble
    log.info(s"Classifier $uuid accuracy: $acc")

    context.system.eventStream.publish(NewModel(acc, classifier.save(), uuid))
  }

  override def receive: Receive = {
    case Train(samples) => fit(samples)
  }
}
