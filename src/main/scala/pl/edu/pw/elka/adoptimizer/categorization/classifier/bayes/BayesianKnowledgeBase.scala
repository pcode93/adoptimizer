package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import pl.edu.pw.elka.adoptimizer.categorization.model.{ Document, FeatureStats, KnowledgeBase, Sample }
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer

import scala.collection.mutable
import scala.math._
import net.liftweb.json._
import net.liftweb.json.JsonDSL._
import java.io._

import scala.io.Source

/**
 * Created by leszek on 26/12/2017.
 */
case class BayesianKnowledgeBase(tk: Tokenizer) extends KnowledgeBase {
  var n: Int = 0
  var c: Int = 0
  var d: Int = 0

  var chisquareCriticalValue: Double = 6.63
  var logPriors: mutable.HashMap[String, Double] = mutable.HashMap[String, Double]()
  var logLikelihoods: mutable.HashMap[String, mutable.HashMap[String, Double]] = mutable.HashMap[String, mutable.HashMap[String, Double]]()

  var tokenizer: Tokenizer = tk

  private def preprocessData(samples: List[Sample]): List[Document] = {
    var dataset: mutable.MutableList[Document] = mutable.MutableList()
    var category: String = ""

    for (sample <- samples) {
      category = sample.category
      val example = sample.content

      var doc = tokenizer.tokenize(example)
      doc.category = category
      dataset += doc
    }
    return dataset.toList
  }

  private def selectFeatures(dataset: List[Document]): FeatureStats = {
    val extractor = FeatureExtraction()

    val stats = extractor.extractFeatureStats(dataset)

    val selectedFeatures = extractor.chisquare(stats, chisquareCriticalValue)

    val filteredFeatures = stats.featureCategoryJointCount.filter(x => selectedFeatures.contains(x._1))
    stats.featureCategoryJointCount = filteredFeatures

    return stats
  }

  override def train(samples: List[Sample]): Unit = {
    val dataset = preprocessData(samples)

    val featureStats = selectFeatures(dataset)

    n += featureStats.n
    d += featureStats.featureCategoryJointCount.size

    c += featureStats.categoryCounts.size

    for (category <- featureStats.categoryCounts) {
      val cat = category._1
      val count = category._2

      logPriors.put(cat, log(count / n.asInstanceOf[Double]))
    }

    val featureOccurrencesInCategory = mutable.HashMap[String, Double]()

    for (category <- logPriors.keys) {
      var featureOccSum = 0.0

      for (categoryListOccurences <- featureStats.featureCategoryJointCount.values) {
        val occurrences = categoryListOccurences.get(category)
        if (occurrences.isDefined)
          featureOccSum += occurrences.get
      }

      featureOccurrencesInCategory.put(category, featureOccSum)
    }

    for (category <- logPriors.keys) {
      for (entry <- featureStats.featureCategoryJointCount) {
        val feature = entry._1
        val featureCategoryCounts = entry._2

        var count = featureCategoryCounts.get(category)
        if (count.isEmpty) {
          count = Some(0)
        }

        val logLikelihood = Math.log((count.get + 1) / (featureOccurrencesInCategory(category) + d))
        if (!logLikelihoods.contains(feature))
          logLikelihoods.put(feature, mutable.HashMap[String, Double]())

        logLikelihoods(feature).put(category, logLikelihood)
      }
    }
  }

  def save(filePath: String): Unit = {
    val njs = ("key" -> "n") ~ ("value" -> n)
    val cjs = ("key" -> "c") ~ ("value" -> c)
    val djs = ("key" -> "d") ~ ("value" -> d)
    val chCV = ("key" -> "chisquareCriticalValue") ~ ("value" -> chisquareCriticalValue)
    val logPr = ("key" -> "logPriors") ~ ("value" -> logPriors)
    val likelihoods = logLikelihoods.toSeq.map(likelyH => ("key" -> "logLikelihoods") ~
      ("word" -> likelyH._1) ~
      ("value" -> likelyH._2))

    val pw = new PrintWriter(new File(filePath))
    pw.write(compactRender(njs) + "\n")
    pw.write(compactRender(cjs) + "\n")
    pw.write(compactRender(djs) + "\n")
    pw.write(compactRender(chCV) + "\n")
    pw.write(compactRender(logPr) + "\n")
    for (i <- likelihoods) {
      pw.write(compactRender(i) + "\n")
    }
    pw.close()
  }

  def load(filePath: String): Unit = {
    implicit val formats = net.liftweb.json.DefaultFormats
    var lines = Source.fromFile(new File(filePath)).getLines()
    for (line <- lines) {
      val json = parse(line)
      val key = (json \ "key").extract[String]

      if (key == "n")
        n = (json \ "value").extract[Int]
      else if (key == "c")
        c = (json \ "value").extract[Int]
      else if (key == "d")
        d = (json \ "value").extract[Int]
      else if (key == "chisquareCriticalValue")
        chisquareCriticalValue = (json \ "value").extract[Double]
      else if (key == "logPriors")
        for (prior <- (json \ "value").extract[Array[Map[String, Double]]]) {
          logPriors.put(prior.head._1, prior.head._2)
        }
      else if (key == "logLikelihoods")
        for (likelyhoods <- (json \ "value").extract[Array[Map[String, Double]]]) {
          val word = (json \ "word").extract[String]
          logLikelihoods.put(word, mutable.HashMap[String, Double]())
          for (likelyhood <- likelyhoods) {
            logLikelihoods(word).put(likelyhood._1, likelyhood._2)
          }
        }
    }
  }
}
