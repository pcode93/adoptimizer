package classifier

import classifier.stemmer.Porter2Stemmer

/**
  * Created by leszek on 27/12/2017.
  */
class SimpleStemmedTokenizer extends SimpleTokenizer {
  val stemmer = new Porter2Stemmer()

  override def tokenize(text: String): Document = {
    val preprocessedText = preprocess(text)
    var keywords = extractKeywords(preprocessedText)

    for (i <- 0 to keywords.length - 1) {
      keywords(i) = stemmer.stem(keywords(i))
    }

    val document = Document()
    document.tokens = getKeywordsCount(keywords)

    return document
  }
}
