package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ Actor, ActorLogging }
import pl.edu.pw.elka.adoptimizer.categorization.ClassifierActor.{ Category, SampleSet }

object ClassifierActor {
  type Category = String
  type SampleSet = Iterable[Sample]
}

final case class Sample(content: Array[String], category: Category)

final case class Classify(sample: String)
final case class Train(samples: SampleSet)

trait ClassifierActor extends Actor with ActorLogging {
  def classify(sample: String)
  def fit(samples: SampleSet)

  override def receive: Receive = {
    case Classify(sample) => classify(sample)
    case Train(samples) => fit(samples)
  }
}
