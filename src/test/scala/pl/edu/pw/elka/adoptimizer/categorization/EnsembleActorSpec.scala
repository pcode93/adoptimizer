package pl.edu.pw.elka.adoptimizer.categorization

import akka.actor.{ ActorRef, ActorSystem, Props }
import akka.testkit.{ TestKit, TestProbe }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpecLike }

import scala.util.Success

class EnsembleActorSpec extends TestKit(ActorSystem("EnsembleSpec"))
    with WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  implicit val timeout = Timeout(5 seconds)

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  val actorHandlingCategoryA = TestProbe()
  val actorHandlingCategoryB = TestProbe()
  val props = Props(new EnsembleActor(
    EnsemblePart(List("A"), actorHandlingCategoryA.ref, 0.5),
    EnsemblePart(List("B"), actorHandlingCategoryB.ref, 0.5)
  ))
  val ensembleActor: ActorRef = system.actorOf(props, "ensembleActor")

  "Ensemble Actor" should {
    "split training data across classifiers" in {
      val samples = List(("text1", "A"), ("text2", "A"), ("text3", "B"))

      ensembleActor ! Train(samples)
      actorHandlingCategoryA.expectMsg(Train(List(("text1", "A"), ("text2", "A"))))
      actorHandlingCategoryB.expectMsg(Train(List(("text3", "B"))))
    }

    "return a weighted sum of scores returned by classifiers" in {
      val sample = ("Test text", "A")
      val future = ensembleActor ? Classify(sample)

      actorHandlingCategoryA.expectMsg(Classify(sample))
      actorHandlingCategoryA.reply(2D)

      whenReady(future) { result =>
        result shouldBe 1D
      }
    }
  }
}
