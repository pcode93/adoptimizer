package pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.NgramTokenizer
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer
import weka.classifiers.functions.Logistic
import weka.core.{DenseInstance, Instances}

class LogisticClassifier(maxNgrams: Int = 1) extends TextClassifier {
  private val lr = new Logistic()
  private var tfIdfVectorizer: TfIdfVectorizer = _

  override def classify(sample: Sample): Double = ???

  override def fit(samples: List[Sample]): Unit = {
    tfIdfVectorizer = new TfIdfVectorizer(samples.map(_.content), NgramTokenizer(1 to maxNgrams))
    val trainingSet = samples.map(sample => (tfIdfVectorizer.vectorize(sample.content), sample.category))
    .map(sample => {
      val instance = new DenseInstance(tfIdfVectorizer.numFeatures)

      sample._1.zipWithIndex.foreach(weight => instance.setValue(weight._2, weight._1))
      instance.setClassValue(sample._2)

      instance
    })
    //lr.buildClassifier(trainingSet)
  }

  override def save(): Any = ???

  override def load(state: Any): Unit = ???
}
