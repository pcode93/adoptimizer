package pl.edu.pw.elka.adoptimizer.categorization.bayes

import classifier.stemmer.Porter2Stemmer
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec }
import pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes.BayesianKnowledgeBase
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.Stopwords
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.SimpleStemmedTokenizer

import scala.collection.mutable
import scala.io.Source

/**
 * Created by leszek on 29/12/2017.
 */
class BayesianKnowledgeBaseSpec extends WordSpec with Matchers with BeforeAndAfterAll {
  var testedKB = BayesianKnowledgeBase(new SimpleStemmedTokenizer())
  var samples = mutable.MutableList[Sample]()
  val testFileName = "testSaveBase"

  override protected def beforeAll(): Unit = {
    val lines = Source.fromResource("trainingData/20ng-train-all-terms.txt").getLines()
    lines.foreach(line => {
      val split = line.split("\t")
      samples += Sample(split(1), split(0))
    })
    testedKB.train(samples.toList)
  }

  "BayesianKnowledgeBase" should {
    "returns 20 categories when trained with sample set" in {
      assert(testedKB.logPriors.size == 20)
    }

    "reduces first provided category into collection without stopwords" in {
      var containsStopwords = false
      val stemmedStopwords = Stopwords.stopwords.mapConserve(x => new Porter2Stemmer().stem(x))
      stemmedStopwords.foreach(x => containsStopwords = testedKB.logLikelihoods.keySet.contains(x))
      assert(!containsStopwords)
    }
  }
}
