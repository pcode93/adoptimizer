package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import classifier.stemmer.Porter2Stemmer
import pl.edu.pw.elka.adoptimizer.categorization.stemmer.Stemmer
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer.Ngrams

import scala.collection.mutable

abstract class NgramTokenizer(ngramRange: Range, separator: String, stopwords: List[String]) extends Tokenizer {

  protected def ngrams(tokens: List[String]): Ngrams = {
    def ngramsIter(n: Int, tokens: List[String], tokensLeft: Int, result: List[String] = List()): List[String] = {
      if (tokens.lengthCompare(n) < 0) result
      else ngramsIter(n, tokens.tail, tokensLeft - 1, tokens.take(n).mkString(" ") :: result)
    }

    (tokens ++ ngramRange.tail.flatMap(ngramsIter(_, tokens, tokens.length)))
      .foldLeft(mutable.Map[String, Int]())((counts, ngram) => {
        counts.update(ngram, counts.getOrElse(ngram, 0) + 1)
        counts
      }).toMap
  }

  protected def unigrams(text: String): List[String] = text.split(separator).toList
  protected def filterStopwords(unigrams: List[String]): List[String] = unigrams.filterNot(stopwords.contains(_))
}

class SimpleNgramTokenizer(ngramRange: Range, separator: String = " ", stopwords: List[String] = List())
    extends NgramTokenizer(ngramRange, separator, stopwords) {
  override def tokenize(text: String): Ngrams = ngrams(filterStopwords(unigrams(text)))
}

class StemmedNgramTokenizer(ngramRange: Range, separator: String = " ",
  stopwords: List[String] = List(), stemmer: Stemmer = new Porter2Stemmer())
    extends NgramTokenizer(ngramRange, separator, stopwords) {
  override def tokenize(text: String): Ngrams =
    ngrams(filterStopwords(unigrams(text)).map(stemmer.stem))
}

class StemmedUnigramTokenizer(stopwords: List[String] = List())
  extends StemmedNgramTokenizer(1 to 1, stopwords = stopwords)
