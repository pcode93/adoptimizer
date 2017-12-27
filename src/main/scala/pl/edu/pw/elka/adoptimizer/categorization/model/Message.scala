package pl.edu.pw.elka.adoptimizer.categorization.model

object Message {
  final case class Classify(sample: Sample)
  final case class Train(samples: List[Sample])
}
