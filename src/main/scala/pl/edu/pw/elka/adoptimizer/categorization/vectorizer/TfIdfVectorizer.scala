package pl.edu.pw.elka.adoptimizer.categorization.vectorizer

import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.{ NgramTokenizer, TfIdfTransformer }

class TfIdfVectorizer(corpus: List[String], tokenizer: NgramTokenizer = NgramTokenizer(1 to 1))
    extends Serializable {
  private val ngrams = corpus.map(tokenizer.tokenize)
  private val features = ngrams.flatMap(_.keys).distinct
  private val transformer = new TfIdfTransformer(ngrams)

  def vectorize(text: String): List[Double] = {
    val counts = transformer.tfidf(tokenizer.tokenize(text))
    features.map(counts.getOrElse(_, 0D))
  }

  val numFeatures: Int = features.length
}
