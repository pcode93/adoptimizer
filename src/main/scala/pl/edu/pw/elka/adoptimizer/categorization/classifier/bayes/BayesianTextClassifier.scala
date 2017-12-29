package pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes

import pl.edu.pw.elka.adoptimizer.categorization.classifier.TextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.Tokenizer

import scala.collection.mutable
import scala.util.control.Breaks._

/**
 * Created by leszek on 26/12/2017.
 */
case class BayesianTextClassifier(tk: Tokenizer) extends TextClassifier {
  final val MinValue: Double = -(2 ^ 32)
  val knowledgeBase = BayesianKnowledgeBase(tk)

  override def classify(sample: Sample): Map[String, Double] = {
    val doc = tk.tokenize(sample.content)

    var results = mutable.Map[String, Double]()

    for (categoryEntry <- knowledgeBase.logPriors.toSeq) {
      val category = categoryEntry._1
      var logprob = MinValue

      for (featureEntry <- doc.tokens.toSeq) {
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

    return results.toMap
  }

  override def fit(samples: List[Sample]): Unit = knowledgeBase.train(samples)

  override def save(): Any = knowledgeBase

  override def load(state: Any): Unit = ???
}
