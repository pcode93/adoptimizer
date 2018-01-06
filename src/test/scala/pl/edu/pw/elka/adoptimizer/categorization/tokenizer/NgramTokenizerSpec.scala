package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import org.scalatest.WordSpecLike

class NgramTokenizerSpec extends WordSpecLike {
  "Ngram tokenizer" should {
    "return correct ngrams for text" in {
      val text = "1 2 1 3 1 2"
      val expectedNgrams = Map(
        "1" -> 3, "2" -> 2, "3" -> 1,
        "1 2" -> 2, "2 1" -> 1, "1 3" -> 1, "3 1" -> 1,
        "1 2 1" -> 1, "2 1 3" -> 1, "1 3 1" -> 1, "3 1 2" -> 1
      )

      val ngrams = new SimpleNgramTokenizer(1 to 3).tokenize(text)

      assert(ngrams == expectedNgrams)
    }
  }
}
