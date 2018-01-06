package pl.edu.pw.elka.adoptimizer.categorization.vectorizer

trait Vectorizer extends Serializable {
  def fit(corpus: List[String])
  def vectorize(text: String): List[Double]
  def numFeatures(): Int
}
