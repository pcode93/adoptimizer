package pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic

import java.util

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.NgramTokenizer
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer
import weka.classifiers.functions.Logistic
import weka.core.{Attribute, DenseInstance, Instances}

class LogisticClassifier(maxNgrams: Int = 1) extends TextClassifier {
  private var coeffs: Array[Array[Double]] = _
  private var tfIdfVectorizer: TfIdfVectorizer = _

  override def classify(sample: Sample): Double = {
    val len = coeffs.length
    val prob = new Array[Double](len)
    val v = new Array[Double](len)
    val counts = tfIdfVectorizer.vectorize(sample.content).toArray

    (1 until (len - 1)).foreach(j => (1 until coeffs(0).length).foreach(k => v(j) += coeffs(k)(j) * counts(k)))
    v(len - 1) = 0

    (1 until len).foreach(m => {
      var sum = 0D
      (1 until (len - 1)).foreach(n => sum += Math.exp(v(n) - v(m)))

      prob(m) = 1 / (sum + Math.exp(-v(m)))
    })

    prob(0)
  }

  override def fit(samples: List[Sample]): Unit = {
    val lr = new Logistic()
    tfIdfVectorizer = new TfIdfVectorizer(samples.map(_.content), NgramTokenizer(1 to maxNgrams))

    val trainingSet = samples
      .map(sample => (tfIdfVectorizer.vectorize(sample.content), sample.category))
      .map(sample => {
        val instance = new DenseInstance(tfIdfVectorizer.numFeatures)

        sample._1.zipWithIndex.foreach(weight => instance.setValue(weight._2, weight._1))
        instance.setClassValue(sample._2)

        instance
      })

    val attrInfo = new util.ArrayList[Attribute]()
    (1 to tfIdfVectorizer.numFeatures).foreach(attr => attrInfo.add(new Attribute(attr.toString)))

    lr.buildClassifier(new Instances("Texts", attrInfo, trainingSet.length))
    coeffs = lr.coefficients()
  }

  override def save(): Any = (coeffs, tfIdfVectorizer)

  override def load(state: Any): Unit = {
    val params = state.asInstanceOf[(Array[Array[Double]], TfIdfVectorizer)]
    coeffs = params._1
    tfIdfVectorizer = params._2
  }
}
