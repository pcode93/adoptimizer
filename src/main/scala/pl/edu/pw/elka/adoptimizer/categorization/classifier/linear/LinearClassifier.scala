package pl.edu.pw.elka.adoptimizer.categorization.classifier.linear

import java.util
import java.util.Date

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.TextFilter
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.Vectorizer
import weka.classifiers.Classifier
import weka.core.{ Attribute, DenseInstance, Instances }

import scala.collection.JavaConverters._

abstract class LinearClassifier[T <: Classifier](protected var vectorizer: Vectorizer)
    extends TextClassifier {
  protected var classifier: T = _
  protected var classIndex: Attribute = _

  protected def createClassifier(): T

  private def debug(text: String) =
    println(s"${new Date()}: $text")

  override def classify(text: String): Map[String, Double] = {
    val features = vectorizer.vectorize(TextFilter.complex.filter(text)).toArray
    val instance = new DenseInstance(1, features)

    classifier.distributionForInstance(instance)
      .zipWithIndex
      .map(scoreForClass => classIndex.value(scoreForClass._2) -> scoreForClass._1)
      .toMap
  }

  override def fit(samples: List[Sample]): Unit = {
    classifier = createClassifier()

    debug("Building corpus")
    vectorizer.fit(samples.map(sample => TextFilter.complex.filter(sample.content)))
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
    samples.foreach(sample => {
      val vec = vectorizer.vectorize(TextFilter.complex.filter(sample.content))
      val instance = new DenseInstance(numFeatures)
      instance.setDataset(instances)

      vec.zipWithIndex.foreach(weight => instance.setValue(weight._2, weight._1))
      instance.setValue(numFeatures - 1, sample.category)

      instances.add(instance)
    })
    debug("Done")

    debug("Building classifier")
    classifier.buildClassifier(instances)
    debug("Done")

    classIndex = instances.classAttribute()
  }

  override def save(): Any = (classifier, vectorizer, classIndex)

  override def load(state: Any): Unit = {
    val params = state.asInstanceOf[(T, Vectorizer, Attribute)]

    classifier = params._1
    vectorizer = params._2
    classIndex = params._3
  }
}
