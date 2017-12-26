package classifier

/**
  * Created by leszek on 26/12/2017.
  */
abstract class Tokenizer {
  def tokenize(text: String): Document
}
