package pl.edu.pw.elka.adoptimizer.api

import akka.actor.{ Actor, ActorLogging, ActorRef, Props }
import pl.edu.pw.elka.adoptimizer.categorization.GenericClassifierActor
import pl.edu.pw.elka.adoptimizer.categorization.classifier.bayes.BayesianTextClassifier
import pl.edu.pw.elka.adoptimizer.categorization.classifier.logistic.LogisticClassifier
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.Train
import pl.edu.pw.elka.adoptimizer.categorization.preprocessing.Stopwords
import pl.edu.pw.elka.adoptimizer.categorization.tokenizer.{ SimpleStemmedTokenizer, StemmedUnigramTokenizer }
import pl.edu.pw.elka.adoptimizer.categorization.util.{ CsvParser, FileReader }
import pl.edu.pw.elka.adoptimizer.categorization.vectorizer.TfIdfVectorizer

object SystemApiActor {
  final case class TrainEnsemble(datasetUri: String)

  def props: Props = Props[SystemApiActor]
}

class SystemApiActor(classificationEnsemble: ActorRef) extends Actor with ActorLogging {
  import SystemApiActor._


  val vectorizer = new TfIdfVectorizer(minCount = 100, maxCount = 1000, tokenizer = new StemmedUnigramTokenizer(Stopwords.en))
  val actor: ActorRef =
    context.actorOf(Props(new GenericClassifierActor(new LogisticClassifier(vectorizer), "lr")), "lrActor")


  def receive: Receive = {
    case TrainEnsemble(uri) =>
      actor ! Train(CsvParser.parse(FileReader.fromPath(uri), ";"))
  }
}
