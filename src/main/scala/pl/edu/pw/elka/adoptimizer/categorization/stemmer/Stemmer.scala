package pl.edu.pw.elka.adoptimizer.categorization.stemmer

trait Stemmer extends Serializable {
  def stem(word: String): String
}
