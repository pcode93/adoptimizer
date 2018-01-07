package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import classifier.stemmer.Porter2Stemmer
import pl.edu.pw.elka.adoptimizer.categorization.stemmer.Stemmer

import scala.collection.mutable

abstract class NgramTokenizer(ngramRange: Range, separator: String, stopwords: List[String]) extends Serializable {

  protected def ngrams(tokens: List[String]): Map[String, Int] = {
    def ngramsIter(n: Int, tokens: List[String], tokensLeft: Int, result: List[String] = List()): List[String] = {
      if (tokens.lengthCompare(n) < 0) result
      else ngramsIter(n, tokens.tail, tokensLeft - 1, tokens.take(n).mkString(" ") :: result)
    }

    val allNgrams = tokens ++ ngramRange.tail.flatMap(ngramsIter(_, tokens, tokens.length))
    val counts = mutable.Map[String, Int]()
    allNgrams.foreach(ngram => counts.update(ngram, counts.getOrElse(ngram, 0) + 1))
    counts.toMap
    //.groupBy(_.toString)
    //.map(ngram => ngram._1 -> ngram._2.length)
  }

  protected def unigrams(text: String): List[String] = text.split(separator).toList
  protected def filterStopwords(unigrams: List[String]): List[String] = unigrams.filterNot(stopwords.contains(_))

  def tokenize(text: String): Map[String, Int]
}

class SimpleNgramTokenizer(ngramRange: Range, separator: String = " ", stopwords: List[String] = List())
    extends NgramTokenizer(ngramRange, separator, stopwords) {
  override def tokenize(text: String): Map[String, Int] = ngrams(filterStopwords(unigrams(text)))
}

class StemmedNgramTokenizer(ngramRange: Range, separator: String = " ",
  stopwords: List[String] = List(), stemmer: Stemmer = new Porter2Stemmer())
    extends NgramTokenizer(ngramRange, separator, stopwords) {
  override def tokenize(text: String): Map[String, Int] =
    ngrams(filterStopwords(unigrams(text)).map(stemmer.stem))
}

class StemmedUnigramTokenizer(stopwords: List[String] = List())
  extends StemmedNgramTokenizer(1 to 1, stopwords = stopwords)
