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

class SystemApiActor extends Actor with ActorLogging {
  import SystemApiActor._


  val vectorizer = new TfIdfVectorizer(minCount = 40, tokenizer = new StemmedUnigramTokenizer(Stopwords.stopwords))
  val actor: ActorRef =
    context.actorOf(Props(new GenericClassifierActor(new LogisticClassifier(vectorizer), "lr")), "lrActor")


  //val actor: ActorRef =
    //context.actorOf(Props(new GenericClassifierActor(BayesianTextClassifier(new SimpleStemmedTokenizer()), "bayes")), "bayesActor")
  def receive: Receive = {
    case TrainEnsemble(uri) =>
      actor ! Train(CsvParser.parse(FileReader.fromPath(uri), ";"))
  }
}
