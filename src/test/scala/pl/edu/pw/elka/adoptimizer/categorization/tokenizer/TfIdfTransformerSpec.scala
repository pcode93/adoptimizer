package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import org.scalatest.WordSpecLike

class TfIdfTransformerSpec extends WordSpecLike {
  "TfIdf transformer" should {
    "correctly fit a training set and transform a testing set" in {

      /*
       TOKEN  |   NUMBER OF DOCS
       1      |   3
       2      |   2
       3      |   2
       4      |   3
       6      |   1
       8      |   1
       */
      val trainingSet = List(
        Map("1" -> 1, "2" -> 1, "3" -> 1),
        Map("1" -> 1, "6" -> 1, "4" -> 1),
        Map("4" -> 1, "2" -> 1, "3" -> 1),
        Map("1" -> 1, "4" -> 1, "8" -> 1)
      )

      val test = List(
        Map("1" -> 2, "2" -> 4, "3" -> 1)
      )
    }
  }
}
