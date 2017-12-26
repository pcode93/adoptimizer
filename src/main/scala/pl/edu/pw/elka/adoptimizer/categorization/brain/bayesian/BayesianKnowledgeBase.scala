package classifier.bayesian

import classifier._

import scala.collection.mutable
import scala.collection.mutable.{Iterable, MutableList}

/**
  * Created by leszek on 26/12/2017.
  */
case class BayesianKnowledgeBase() extends KnowledgeBase {
  var n = 0
  var c = 0
  var d = 0

  var chisquareCriticalValue = 6.63
  var logPriors = mutable.HashMap[String, Double]()
  var logLikelihoods = mutable.HashMap[String, mutable.HashMap[String, Double]]()

  var tokenizer: Tokenizer = SimpleTokenizer()

  private def preprocessData(samples: Array[Sample]): List[Document] = {
    var dataset: MutableList[Document] = MutableList()
    var category: String = ""

    for (sample <- samples) {
      category = sample.category
      var examples = sample.content

      for (example <- examples) {
        var doc = tokenizer.tokenize(example)
        doc.category = category
        dataset += doc
      }
    }
    return dataset.toList
  }

  private def selectFeatures(dataset: List[Document]): FeatureStats = {
    var extractor = FeatureExtraction()

    var stats = extractor.extractFeatureStats(dataset)

    var selectedFeatures = extractor.chisquare(stats, chisquareCriticalValue)

    var filteredFeatures = stats.featureCategoryJointCount.filter( x => selectedFeatures.contains(x._1) )
    stats.featureCategoryJointCount = filteredFeatures

    return stats
  }

  override def train(samples: Array[Sample]): Unit = {
    var dataset = preprocessData(samples)

    var featureStats = selectFeatures(dataset)

    n = featureStats.n
    d = featureStats.featureCategoryJointCount.size

//    if (categoryPriors == null) {
      c = featureStats.categoryCounts.size
      logPriors = mutable.HashMap()

      for (category <- featureStats.categoryCounts) {
        var cat = category._1
        var count = category._2

        logPriors.put(cat, Math.log(count / n))
      }
//    }
//    else {
//      c = categoryPriors.size
//
//      if (c != featureStats.categoryCounts.size) {
//        throw new Exception()
//      }
//
//      for (cat <- categoryPriors) {
//        var category = cat._1
//        var priorProbability = cat._2
//
//        logPriors.put(category, Math.log(priorProbability))
//      }
//    }

    var featureOccurrencesInCategory = mutable.HashMap[String, Double]()

    for (category <- logPriors.keys) {
      var featureOccSum = 0.0

      for (categoryListOccurences <- featureStats.featureCategoryJointCount.values) {
        var occurrences = categoryListOccurences.get(category)
        if (occurrences.isDefined)
          featureOccSum += occurrences.get
      }

      featureOccurrencesInCategory.put(category, featureOccSum)
    }


    for (category <- logPriors.keys) {
      for (entry <- featureStats.featureCategoryJointCount) {
        var feature = entry._1
        var featureCategoryCounts = entry._2

        var count = featureCategoryCounts.get(category)
        if (count.isEmpty) { count = Some(0) }

        var logLikelihood = Math.log((count.get+1) / (featureOccurrencesInCategory(category) + d))
        if (!logLikelihoods.contains(feature))
          logLikelihoods.put(feature, mutable.HashMap[String, Double]())

        logLikelihoods(feature).put(category, logLikelihood)
      }
    }
  }
}
