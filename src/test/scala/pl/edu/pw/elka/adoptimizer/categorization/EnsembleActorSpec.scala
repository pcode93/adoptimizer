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

  val actorA = TestProbe()
  val actorB = TestProbe()

  val props = Props(new EnsembleActor(
    EnsemblePart(actorA.ref, 0.5),
    EnsemblePart(actorB.ref, 0.5)
  ))

  val ensembleActor: ActorRef = system.actorOf(props, "ensembleActor")

  "Ensemble Actor" should {
    "send training data to classifiers" in {
      val samples = List(Sample("text1", "A"), Sample("text2", "B"))

      ensembleActor ! Train(samples)
      actorA.expectMsg(Train(samples))
      actorB.expectMsg(Train(samples))
    }

    "return a weighted sum of scores returned by classifiers" in {
      val sample = Sample("Test text", "A")
      val future = ensembleActor ? Classify(sample)

      actorA.expectMsg(Classify(sample))
      actorA.reply(2D)

      actorB.expectMsg(Classify(sample))
      actorB.reply(2D)

      whenReady(future) { result =>
        result shouldBe 2D
      }
    }
  }
}
