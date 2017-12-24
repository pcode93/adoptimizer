package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.categorization.ClassifierActor.{ Category, SampleSet }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final case class EnsemblePart(classes: List[Category], ref: ActorRef, weight: Double)
final case class EnsembleClassifierException(msg: String, cause: Throwable) extends RuntimeException

object EnsembleActor {
  def props: Props = Props[EnsembleActor]
}

class EnsembleActor(classifiers: EnsemblePart*) extends ClassifierActor {
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = context.dispatcher

  override def classify(sample: Sample): Unit = {
    val replyTo = sender()
    val classifications = classifiers
      .filter(_.classes.contains(sample.category))
      .map(classifier => (classifier.ref ? Classify(sample))
        .mapTo[Double]
        .map(score => (classifier.weight, score)))

    Future.sequence(classifications).onComplete {
      case Success(scores) => replyTo ! scores.foldLeft(0D)((sum, score) => sum + score._1 * score._2)
      case Failure(exception) =>
        throw EnsembleClassifierException("Exception when classifying", exception)
    }
  }

  override def fit(samples: SampleSet): Unit = {
    val classes = samples.groupBy(_.category)
    classifiers.foreach(classifier => {
      val filteredClasses = classes.filter(clazz => classifier.classes.contains(clazz._1))
      classifier.ref ! Train(filteredClasses.flatMap(_._2).toList)
    })
  }
}
