package classifier.bayesian

import classifier.Document

import scala.collection.mutable

/**
  * Created by leszek on 26/12/2017.
  */
case class FeatureExtraction() {
  def extractFeatureStats(dataset: List[Document]): FeatureStats = {
    val stats = FeatureStats()

    var categoryCount: Option[Int] = None
    var category = ""
    var featureCategoryCount: Option[Int] = None
    var feature = ""
    var featureCategoryCounts: Option[mutable.Map[String, Int]] = None

    for (doc <- dataset) {
      stats.n += 1

      category = doc.category
      categoryCount = stats.categoryCounts.get(category)
      if (categoryCount.isEmpty)
        stats.categoryCounts.put(category, 1)
      else
        stats.categoryCounts.put(category, categoryCount.get + 1)

      for (entry <- doc.tokens.toSeq) {
        feature = entry._1

        featureCategoryCounts = stats.featureCategoryJointCount.get(feature)
        if (featureCategoryCounts.isEmpty) {
          stats.featureCategoryJointCount.put(feature, mutable.Map[String, Int]())
          featureCategoryCounts = stats.featureCategoryJointCount.get(feature)
        }

        featureCategoryCount = featureCategoryCounts.get.get(category)
        if (featureCategoryCount.isEmpty)
          featureCategoryCount = Some(0)

        stats.featureCategoryJointCount.get(feature).get.put(category, featureCategoryCount.get + 1)
      }
    }
    return stats
  }

  def chisquare(stats: FeatureStats, criticalLevel: Double): mutable.Map[String, Double] = {
    val selectedFeatures = mutable.HashMap[String, Double]()

    var feature: String = ""
    var category: String = ""
    var categoryList = mutable.Map[String, Int]()

    var N1dot = 0
    var N0dot = 0
    var N00 = 0
    var N01 = 0
    var N10 = 0
    var N11 = 0
    var chisquareScore = .0
    var previousScore: Option[Double] = None

    for (entry1 <- stats.featureCategoryJointCount.toSeq) {
      feature = entry1._1
      categoryList = entry1._2

      N1dot = 0
      for (count <- categoryList.values) {
        N1dot += count
      }

      N0dot = stats.n - N1dot


      for (entry2 <- categoryList.toSeq) {
        category = entry2._1
        N11 = entry2._2

        N01 = stats.categoryCounts(category) - N11

        N00 = N0dot - N01
        N10 = N1dot - N11

        chisquareScore = stats.n * Math.pow(N11 * N00 - N10 * N01, 2) / ((N11 + N01) * (N11 + N10) * (N10 + N00) * (N01 + N00))

        if (chisquareScore >= criticalLevel) {
          previousScore = selectedFeatures.get(feature)
          if (previousScore.isEmpty || chisquareScore > previousScore.get)
            selectedFeatures.put(feature, chisquareScore)
        }
      }
    }
    return selectedFeatures
  }
}
