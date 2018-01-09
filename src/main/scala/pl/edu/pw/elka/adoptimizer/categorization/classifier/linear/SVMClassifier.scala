package pl.edu.pw.elka.adoptimizer.categorization.classifier.linear

import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.Vectorizer
import weka.classifiers.functions.SMO

class SVMClassifier(vectorizer: Vectorizer, c: Double = 1D) extends LinearClassifier[SMO](vectorizer) {
  override protected def createClassifier(): SMO = {
    val smo = new SMO()
    smo.setC(c)
    smo
  }
}
