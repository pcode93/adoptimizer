package classifier
/**
  * Created by leszek on 26/12/2017.
  */

abstract class TextClassifier {
  def classify(text: String): String
}

