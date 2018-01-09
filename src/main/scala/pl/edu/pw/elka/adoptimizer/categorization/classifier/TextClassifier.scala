package pl.edu.pw.elka.adoptimizer.categorization.classifier

import pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes.BayesianTextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.classifier.linear.LogisticClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.Stopwords
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.StemmedUnigramTokenizer
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer

/**
 * Created by leszek on 26/12/2017.
 */

trait TextClassifier {
  def classify(text: String): Map[String, Double]
  def fit(samples: List[Sample]): Unit
  def save(): Any
  def load(state: Any): Unit
}

object TextClassifier {
  def of(name: String, params: Map[String, String] = null): TextClassifier = {
    name match {
      case "lr" =>
        val vectorizer = new TfIdfVectorizer(minCount = 100, maxCount = 1000, tokenizer = new StemmedUnigramTokenizer(Stopwords.en))
        new LogisticClassifier(vectorizer)
      case "bayes" => BayesianTextClassifier(new StemmedUnigramTokenizer(Stopwords.en))
    }
  }
}

