package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

class TfIdfTransformer(corpus: Seq[Map[String, Int]]) extends Serializable {
  private val inverseDocFrequency = corpus.flatMap(_.map(ngram => (ngram._1, ngram._2)))
    .groupBy(_._1).map(ngram => ngram._1 -> ngram._2.length)

  def tfidf(ngrams: Map[String, Int]): Map[String, Double] =
    ngrams.map(ngram => ngram._1 -> ((ngram._2.toDouble + 1D) / (inverseDocFrequency.getOrElse(ngram._1, 0) + 1)))

  def tfidf(dataset: Seq[Map[String, Int]]): Seq[Map[String, Double]] = dataset.map(tfidf)
}
