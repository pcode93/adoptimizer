package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import scala.collection.mutable
import scala.collection.mutable.Map

/**
 * Created by leszek on 26/12/2017.
 */
case class FeatureStats() {
  /**
   * total number of Observations
   */
  var n = 0
  /**
   * It stores the co-occurrences of Feature and Category values
   */
  var featureCategoryJointCount: Map[String, Map[String, Int]] = mutable.Map()
  /**
   * Measures how many times each category was found in the training dataset.
   */
  var categoryCounts: Map[String, Int] = mutable.Map()
}
