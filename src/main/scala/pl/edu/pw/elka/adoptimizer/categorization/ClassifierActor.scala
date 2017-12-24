package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ Actor, ActorLogging }
import pl.edu.pw.elka.adoptimizer.categorization.ClassifierActor.{ Sample, SampleSet }

object ClassifierActor {
  type Category = String
  type Sample = (String, Category)
  type SampleSet = Iterable[Sample]
}

final case class Classify(sample: Sample)
final case class Train(samples: SampleSet)

trait ClassifierActor extends Actor with ActorLogging {
  def classify(sample: Sample)
  def fit(samples: SampleSet)

  override def receive: Receive = {
    case Classify(sample) => classify(sample)
    case Train(samples) => fit(samples)
  }
}
