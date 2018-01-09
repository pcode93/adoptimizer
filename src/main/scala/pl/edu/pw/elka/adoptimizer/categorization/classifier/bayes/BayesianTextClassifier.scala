package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.TextFilter
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer

import scala.collection.mutable
import scala.util.control.Breaks._

/**
 * Created by leszek on 26/12/2017.
 */
case class BayesianTextClassifier(tk: Tokenizer) extends TextClassifier {
  final val MinValue: Double = -(2 ^ 32)
  var knowledgeBase: BayesianKnowledgeBase = _

  override def classify(text: String): Map[String, Double] = {
    val tokens = tk.tokenize(TextFilter.complex.filter(text))

    var results = mutable.Map[String, Double]()

    for (categoryEntry <- knowledgeBase.logPriors.toSeq) {
      val category = categoryEntry._1
      var logprob = MinValue

      for (featureEntry <- tokens.toSeq) {
        breakable {
          val feature = featureEntry._1

          if (!knowledgeBase.logLikelihoods.contains(feature)) {
            break
          }

          val occurrences = featureEntry._2

          logprob += occurrences * knowledgeBase.logLikelihoods(feature)(category)
        }
      }

      results.put(category, logprob)
    }

    results.toMap
  }

  override def fit(samples: List[Sample]): Unit = {
    knowledgeBase = BayesianKnowledgeBase(tk)
    knowledgeBase.train(samples)
  }

  override def save(): Any = knowledgeBase

  override def load(state: Any): Unit = knowledgeBase = state.asInstanceOf[BayesianKnowledgeBase]
}
