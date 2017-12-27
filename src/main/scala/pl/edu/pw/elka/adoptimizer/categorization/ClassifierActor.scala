package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{Actor, ActorLogging}
import classifier._
import classifier.bayesian.{BayesianKnowledgeBase, BayesianTextClassifier}

import scala.collection.mutable
import scala.io.Source

final case class Classify(sample: String)
final case class Train(samples: Array[Sample])

trait ClassifierActor extends Actor with ActorLogging {
  var tokenizer: Tokenizer = new SimpleStemmedTokenizer()
  var kb: BayesianKnowledgeBase = BayesianKnowledgeBase(tokenizer)
  var classifier: Option[TextClassifier] = None

  def classify(sample: String): Unit = {
    if (classifier.isDefined) {
      val classificationResult = classifier.get.classify(sample)
      //todo: handle classification result
    }
    //todo: handle classificator setup error
  }

  def fit(samples: Array[Sample]): Unit = {
    kb.train(samples)
    classifier = Some(BayesianTextClassifier(kb, tokenizer))
  }

  override def receive: Receive = {
    case Classify(sample) => classify(sample)
    case Train(samples) => fit(samples)
  }

  def trainWithFile(path: String): Unit = {
    val source = Source.fromFile(path)
    val trainingExamples = new mutable.MutableList[Sample]
    for (line <- source.getLines) {
      val separatedLine = line.split("\t")
      trainingExamples += Sample(separatedLine(1), separatedLine(0))
    }
    fit(trainingExamples.toArray)
  }
}
