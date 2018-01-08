package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.{ Classify, Train }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final case class EnsemblePart(ref: ActorRef, weight: Double = 1D)
final case class EnsembleClassifierException(msg: String, cause: Throwable) extends RuntimeException

object EnsembleActor {
  def props: Props = Props[EnsembleActor]
}

class EnsembleActor(classifiers: EnsemblePart*) extends Actor {
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = context.dispatcher

  def classify(sample: Sample, sender: ActorRef): Unit = {
    val classifications = classifiers
      .map(classifier => (classifier.ref ? Classify(sample))
        .mapTo[Double]
        .map(score => (classifier.weight, score)))

    Future.sequence(classifications).onComplete {
      case Success(scores) => sender ! scores.foldLeft(0D)((sum, score) => sum + score._1 * score._2)
      case Failure(exception) =>
        throw EnsembleClassifierException("Exception when classifying", exception)
    }
  }

  def fit(samples: List[Sample]): Unit = {
    classifiers.foreach(_.ref ! Train(samples))
  }

  override def receive: Receive = {
    case Classify(sample) => classify(sample, sender())
    case Train(samples) => fit(samples)
  }
}
