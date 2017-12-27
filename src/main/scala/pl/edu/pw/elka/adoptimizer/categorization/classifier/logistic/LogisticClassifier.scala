package pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import weka.classifiers.functions.Logistic
import weka.core.DenseInstance

class LogisticClassifier extends TextClassifier {
  val lr = new Logistic()

  override def classify(sample: Sample): Double = ???

  override def fit(samples: List[Sample]): Unit = ???

  override def save(): Any = ???

  override def load(state: Any): Unit = ???
}
