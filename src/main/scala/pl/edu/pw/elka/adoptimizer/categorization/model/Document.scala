package pl.edu.pw.elka.adoptimizer.categorization.model

import scala.collection.mutable

/**
 * Created by leszek on 26/12/2017.
 */
case class Document() {
  var tokens: mutable.Map[String, Int] = mutable.Map[String, Int]()
  var category: String = ""
}