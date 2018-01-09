package pl.edu.pw.elka.adoptimizer.categorization.model

import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer.Ngrams

/**
 * Created by leszek on 26/12/2017.
 */
case class Document(tokens: Ngrams, category: String) {}