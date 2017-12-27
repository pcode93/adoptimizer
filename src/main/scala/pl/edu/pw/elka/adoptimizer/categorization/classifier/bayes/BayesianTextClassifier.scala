package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer

import scala.util.control.Breaks._

/**
 * Created by leszek on 26/12/2017.
 */
case class BayesianTextClassifier(tk: Tokenizer) extends TextClassifier {
  val knowledgeBase = BayesianKnowledgeBase(tk)

  override def classify(sample: Sample): Double = {
    var doc = tk.tokenize(sample.content)

    var category: String = ""
    var feature: String = ""
    var occurrences: Int = 0
    var logprob: Double = 0.0

    var maxScoreCategory = ""
    var maxScore: Double = Double.MinValue

    for (categoryEntry <- knowledgeBase.logPriors.toSeq) {
      category = categoryEntry._1
      logprob = -(2 ^ 32) //Bug with -Infinite

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

    return 0D
  }

  override def fit(samples: List[Sample]): Unit = knowledgeBase.train(samples)

  override def save(): Any = knowledgeBase

  override def load(state: Any): Unit = ???
}
