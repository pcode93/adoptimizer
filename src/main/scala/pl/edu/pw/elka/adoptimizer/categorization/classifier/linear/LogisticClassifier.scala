package pl.edu.pw.elka.adoptimizer.categorization.classifier.linear

import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.Vectorizer
import weka.classifiers.functions.Logistic

class LogisticClassifier(vectorizer: Vectorizer, maxIterations: Int = 20)
    extends LinearClassifier[Logistic](vectorizer) {
  override def createClassifier(): Logistic = {
    val lr = new Logistic()

    lr.setUseConjugateGradientDescent(true)
    lr.setMaxIts(maxIterations)

    lr
  }
}
