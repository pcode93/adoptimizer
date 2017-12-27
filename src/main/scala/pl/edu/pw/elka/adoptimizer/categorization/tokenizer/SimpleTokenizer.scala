package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import pl.edu.pw.elka.adoptimizer.categorization.model.Document

import scala.collection.mutable

/**
 * Created by leszek on 26/12/2017.
 */
case class SimpleTokenizer() extends Tokenizer {
  protected def preprocess(text: String): String = {
    return text.replaceAll("\\p{P}", " ").replaceAll("\\s+", " ").toLowerCase()
  }

  protected def extractKeywords(string: String): Array[String] = {
    return string.split(" ")
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

    val document = Document()
    document.tokens = getKeywordsCount(keywords)

    return document
  }
}
