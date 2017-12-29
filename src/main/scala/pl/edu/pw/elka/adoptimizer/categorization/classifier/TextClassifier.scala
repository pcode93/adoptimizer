package pl.edu.pw.elka.adoptimizer.categorization.classifier

import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

/**
 * Created by leszek on 26/12/2017.
 */

trait TextClassifier {
  def classify(sample: Sample): Map[String, Double]
  def fit(samples: List[Sample]): Unit
  def save(): Any
  def load(state: Any): Unit
}

