package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.pattern.ask
import akka.testkit.{ TestKit, TestProbe }
import akka.util.Timeout
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }
import pl.edu.pw.elka.adoptimizer.categorization.model.Message.{ Classify, Train }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

import scala.concurrent.duration._

class EnsembleActorSpec extends TestKit(ActorSystem("EnsembleSpec"))
    with WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }
/*
  val actorHandlingCategoryA = TestProbe()
  val actorHandlingCategoryB = TestProbe()
  val props = Props(new EnsembleActor(
    EnsemblePart(List("A"), actorHandlingCategoryA.ref, 0.5),
    EnsemblePart(List("B"), actorHandlingCategoryB.ref, 0.5)
  ))
  val ensembleActor: ActorRef = system.actorOf(props, "ensembleActor")

  "Ensemble Actor" should {
    "split training data across classifiers" in {
      val samplesFromA = List(Sample("text1", "A"), Sample("text2", "A"))
      val samplesFromB = List(Sample("text3", "B"))
      val allSamples = samplesFromA ++ samplesFromB

      ensembleActor ! Train(allSamples)
      actorHandlingCategoryA.expectMsg(Train(samplesFromA))
      actorHandlingCategoryB.expectMsg(Train(samplesFromB))
    }

    "return a weighted sum of scores returned by classifiers" in {
      val sample = Sample("Test text", "A")
      val future = ensembleActor ? Classify(sample)

      actorHandlingCategoryA.expectMsg(Classify(sample))
      actorHandlingCategoryA.reply(Map((sample.category, 2D)))

      whenReady(future) { result =>
        result shouldBe 1D
      }
    }
  }
  */
}
