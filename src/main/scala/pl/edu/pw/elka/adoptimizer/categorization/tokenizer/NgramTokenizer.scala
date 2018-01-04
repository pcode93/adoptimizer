package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

case class NgramTokenizer(ngramRange: Range, separator: String = " ") {

  protected def ngrams(tokens: List[String]): Map[String, Int] = {
    def ngramsIter(n: Int, tokens: List[String], tokensLeft: Int, result: List[String] = List()): List[String] = {
      if (tokens.lengthCompare(n) < 0) result
      else ngramsIter(n, tokens.tail, tokensLeft - 1, tokens.take(n).mkString(" ") :: result)
    }

    (tokens ++ ngramRange.tail.flatMap(ngramsIter(_, tokens, tokens.length)))
      .groupBy(_.toString)
      .map(ngram => ngram._1 -> ngram._2.length)
  }

  def tokenize(text: String): Map[String, Int] = {
    ngrams(text.split(separator).toList)
  }
}
