package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import pl.edu.pw.elka.adoptimizer.categorization.model.Document
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.Stopwords

import scala.collection.mutable

case class SimpleTokenizer() extends Tokenizer {
  protected def preprocess(text: String): String = {
    return text.replaceAll("\\p{P}", " ").replaceAll("\\s+", " ").toLowerCase()
  }

  protected def extractKeywords(string: String): Array[String] = {
    return string.split(" ")
  }

  protected def removeStopwords(keywords: Array[String]): Array[String] = {
    return keywords.filter(x => !Stopwords.stopwords.contains(x))
  }

  protected def getKeywordsCount(keywords: Array[String]): mutable.Map[String, Int] = {
    val counts = mutable.Map[String, Int]()
    var counter: Option[Int] = null

    for (key <- keywords) {
      counter = counts.get(key)
      if (counter.isEmpty) { counter = Some(0) }

      counts.put(key, counter.get + 1)
    }

    return counts
  }

  def tokenize(text: String): Document = {
    val preprocessedText = preprocess(text)
    val keywords = extractKeywords(preprocessedText)
    val reducedKeywords = removeStopwords(keywords)

    val document = Document()
    document.tokens = getKeywordsCount(reducedKeywords)

    return document
  }
}
