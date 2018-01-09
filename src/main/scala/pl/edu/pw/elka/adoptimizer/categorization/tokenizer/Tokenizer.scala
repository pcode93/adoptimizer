package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer.Ngrams

object Tokenizer {
  type Ngrams = Map[String, Int]
}

trait Tokenizer extends Serializable {
  def tokenize(text: String): Ngrams
}
