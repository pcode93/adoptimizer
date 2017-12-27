package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import pl.edu.pw.elka.adoptimizer.categorization.model.Document

/**
 * Created by leszek on 26/12/2017.
 */
trait Tokenizer {
  def tokenize(text: String): Document
}
