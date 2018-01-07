package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import pl.edu.pw.elka.adoptimizer.categorization.model.Document

trait Tokenizer {
  def tokenize(text: String): Document
}
