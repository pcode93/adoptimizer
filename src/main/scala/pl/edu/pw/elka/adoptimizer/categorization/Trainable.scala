package pl.edu.pw.elka.adoptimizer.categorization

import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

trait Trainable {
  def fit(samples: List[Sample]): Unit
}
