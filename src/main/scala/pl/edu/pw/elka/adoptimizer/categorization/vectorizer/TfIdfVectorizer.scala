package pl.edu.pw.elka.adoptimizer.categorization.vectorizer

import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.{ NgramTokenizer, StemmedUnigramTokenizer, TfIdfTransformer }

class TfIdfVectorizer(
    tokenizer: NgramTokenizer = new StemmedUnigramTokenizer(),
    minCount: Int = 0, maxCount: Int = Int.MaxValue
) extends Vectorizer {

  private var ngrams: List[Map[String, Int]] = _
  private var features: List[String] = _
  private var transformer: TfIdfTransformer = _

  override def vectorize(text: String): List[Double] = {
    val counts = transformer.tfidf(tokenizer.tokenize(text))
    features.map(counts.getOrElse(_, 0D))
  }

  var numFeatures: Int = 0

  override def fit(corpus: List[String]): Unit = {
    ngrams = corpus.map(tokenizer.tokenize)

    val ngramCounts = ngrams.flatMap(_.map(ngram => (ngram._1, ngram._2)))
      .groupBy(_._1).map(ngram => ngram._1 -> ngram._2.map(_._2).sum)

    ngrams = ngrams.map(_.filter(ngram => {
      val count = ngramCounts.getOrElse(ngram._1, 0)
      count >= minCount && count <= maxCount
    }))

    features = ngrams.flatMap(_.keys).distinct
    numFeatures = features.length
    transformer = new TfIdfTransformer(ngrams)
  }
}
