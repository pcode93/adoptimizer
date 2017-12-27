package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ Actor, ActorRef, Props }
import akka.pattern.ask
import akka.util.Timeout
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.{ Classify, Train }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Failure, Success }

final case class EnsemblePart(classes: List[String], ref: ActorRef, weight: Double)
final case class EnsembleClassifierException(msg: String, cause: Throwable) extends RuntimeException

object EnsembleActor {
  def props: Props = Props[EnsembleActor]
}

class EnsembleActor(classifiers: EnsemblePart*) extends Actor {
  implicit val timeout = Timeout(5 seconds)
  implicit val executionContext = context.dispatcher

  def classify(sample: Sample, sender: ActorRef): Unit = {
    val classifications = classifiers
      .filter(_.classes.contains(sample.category))
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
    val classes = samples.groupBy(_.category)
    classifiers.foreach(classifier => {
      val filteredClasses = classes.filter(clazz => classifier.classes.contains(clazz._1))
      classifier.ref ! Train(filteredClasses.flatMap(_._2).toList)
    })
  }

  override def receive: Receive = {
    case Classify(sample) => classify(sample, sender())
    case Train(samples) => fit(samples)
  }
}
