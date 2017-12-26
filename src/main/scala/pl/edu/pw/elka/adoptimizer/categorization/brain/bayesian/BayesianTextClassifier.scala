package classifier.bayesian

import classifier.{KnowledgeBase, TextClassifier, Tokenizer}
import util.control.Breaks._

/**
  * Created by leszek on 26/12/2017.
  */
case class BayesianTextClassifier(kB: BayesianKnowledgeBase, tk: Tokenizer) extends TextClassifier {
  var knowledgeBase: BayesianKnowledgeBase = kB
  var tokenizer: Tokenizer = tk

  override def classify(text: String): String = {
    var doc = tk.tokenize(text)

    var category: String = ""
    var feature: String = ""
    var occurrences: Int = 0
    var logprob: Double = 0.0

    var maxScoreCategory = ""
    var maxScore: Double = Double.MinValue

    for (categoryEntry <- knowledgeBase.logPriors.toSeq) {
      category = categoryEntry._1
      logprob = -(2^32) //Bug with -Infinite

      for (featureEntry <- doc.tokens.toSeq) {
        breakable {
          feature = featureEntry._1

          if (!knowledgeBase.logLikelihoods.contains(feature)) {
            break
          }

          occurrences = featureEntry._2

          logprob += occurrences * knowledgeBase.logLikelihoods(feature)(category)
        }
      }

      if (logprob > maxScore) {
        maxScore = logprob
        maxScoreCategory = category
      }
    }

    return maxScoreCategory
  }
}
