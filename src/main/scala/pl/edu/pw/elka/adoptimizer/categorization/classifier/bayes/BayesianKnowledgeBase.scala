package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import pl.edu.pw.elka.adoptimizer.categorization.model.{ Document, KnowledgeBase, Sample }
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer

import scala.collection.mutable

/**
 * Created by leszek on 26/12/2017.
 */
case class BayesianKnowledgeBase(tk: Tokenizer) extends KnowledgeBase {
  var n = 0
  var c = 0
  var d = 0

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

      logPriors.put(cat, Math.log(count / n))
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
        if (count.isEmpty) { count = Some(0) }

        val logLikelihood = Math.log((count.get + 1) / (featureOccurrencesInCategory(category) + d))
        if (!logLikelihoods.contains(feature))
          logLikelihoods.put(feature, mutable.HashMap[String, Double]())

        logLikelihoods(feature).put(category, logLikelihood)
      }
    }
  }
}
