package pl.edu.pw.elka.adoptimizer.categorization.tokenizer

import classifier.stemmer.Porter2Stemmer
import pl.edu.pw.elka.adoptimizer.categorization.model.Document

class SimpleStemmedTokenizer extends SimpleTokenizer {
  val stemmer = new Porter2Stemmer()

  protected def stemKeywords(keywords: Array[String]): Array[String] = {
    return keywords.map(x => stemmer.stem(x))
  }

  override def tokenize(text: String): Document = {
    val preprocessedText = preprocess(text)
    val keywords = extractKeywords(preprocessedText)
    val reducedKeywords = removeStopwords(keywords)
    val stemmedKeywords = stemKeywords(reducedKeywords)

    val document = Document()
    document.tokens = getKeywordsCount(stemmedKeywords)

    return document
  }
}
