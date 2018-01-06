package pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic

import java.util
import java.util.Date

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.{ ComplexTextFilter, TextCleaningFilter, WhitespaceConvertingFilter }
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.Vectorizer
import weka.classifiers.functions.Logistic
import weka.core.{ Attribute, DenseInstance, Instances }

import scala.collection.JavaConverters._

class LogisticClassifier(var vectorizer: Vectorizer) extends TextClassifier {
  private val textFilter = ComplexTextFilter(WhitespaceConvertingFilter(), TextCleaningFilter())

  private var lr: Logistic = _
  private var classIndex: Attribute = _

  private def debug(text: String) =
    println(s"${new Date()}: $text")

  override def classify(sample: Sample): Double = {
    val features = vectorizer.vectorize(textFilter.filter(sample.content)).toArray
    val instance = new DenseInstance(1, features)

    lr.distributionForInstance(instance)
      .zipWithIndex
      .find(_._2 == classIndex.indexOfValue(sample.category))
      .map(_._1).getOrElse(0D)
  }

  override def fit(samples: List[Sample]): Unit = {
    lr = new Logistic()

    debug("Building corpus")
    vectorizer.fit(samples.map(sample => textFilter.filter(sample.content)))
    debug("Done")

    val numFeatures = vectorizer.numFeatures + 1
    val labels = samples.map(_.category).distinct

    debug(s"Number of features: $numFeatures")
    debug(s"Number of classes: ${labels.length}")

    val attrInfo = new util.ArrayList[Attribute](numFeatures)
    (1 to vectorizer.numFeatures).foreach(attr => attrInfo.add(new Attribute(attr.toString)))
    attrInfo.add(new Attribute("label", labels.asJava))

    val instances = new Instances("training set", attrInfo, samples.length)
    instances.setClassIndex(instances.numAttributes() - 1)

    debug("Vectorizing samples")
    samples
      .map(sample => (vectorizer.vectorize(sample.content), sample.category))
      .foreach(sample => {
        val instance = new DenseInstance(numFeatures)
        instance.setDataset(instances)

        sample._1.zipWithIndex.foreach(weight => instance.setValue(weight._2, weight._1))
        instance.setValue(numFeatures - 1, sample._2)

        instances.add(instance)
      })
    debug("Done")

    debug("Building classifier")
    lr.buildClassifier(instances)
    debug("Done")
    classIndex = instances.classAttribute()
  }

  override def save(): Any = (lr, vectorizer, classIndex)

  override def load(state: Any): Unit = {
    val params = state.asInstanceOf[(Logistic, Vectorizer, Attribute)]

    lr = params._1
    vectorizer = params._2
    classIndex = params._3
  }
}
