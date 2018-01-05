package pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic

import java.util

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.NgramTokenizer
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer
import weka.classifiers.functions.Logistic
import weka.core.{ Attribute, DenseInstance, Instances }

import scala.collection.JavaConverters._

class LogisticClassifier(maxNgrams: Int = 1) extends TextClassifier {
  private var lr: Logistic = _
  private var tfIdfVectorizer: TfIdfVectorizer = _
  private var classIndex: Attribute = _

  override def classify(sample: Sample): Double = {
    val features = tfIdfVectorizer.vectorize(sample.content).toArray
    val instance = new DenseInstance(1, features)

    lr.distributionForInstance(instance)
      .zipWithIndex
      .find(_._2 == classIndex.indexOfValue(sample.category))
      .map(_._1).getOrElse(0D)
  }

  override def fit(samples: List[Sample]): Unit = {
    lr = new Logistic()
    tfIdfVectorizer = new TfIdfVectorizer(samples.map(_.content), NgramTokenizer(1 to maxNgrams))

    val numFeatures = tfIdfVectorizer.numFeatures + 1
    val labels = samples.map(_.category).distinct

    val attrInfo = new util.ArrayList[Attribute](numFeatures)
    (1 to tfIdfVectorizer.numFeatures).foreach(attr => attrInfo.add(new Attribute(attr.toString)))
    attrInfo.add(new Attribute("label", labels.asJava))

    val instances = new Instances("training set", attrInfo, samples.length)
    instances.setClassIndex(instances.numAttributes() - 1)

    samples
      .map(sample => (tfIdfVectorizer.vectorize(sample.content), sample.category))
      .foreach(sample => {
        val instance = new DenseInstance(numFeatures)

        sample._1.zipWithIndex.foreach(weight => instance.setValue(weight._2, weight._1))
        instance.setValue(numFeatures - 1, sample._2)

        instances.add(instance)
      })

    lr.buildClassifier(instances)
    classIndex = instances.classAttribute()
  }

  override def save(): Any = (lr, tfIdfVectorizer, classIndex)

  override def load(state: Any): Unit = {
    val params = state.asInstanceOf[(Logistic, TfIdfVectorizer, Attribute)]

    lr = params._1
    tfIdfVectorizer = params._2
    classIndex = params._3
  }
}
