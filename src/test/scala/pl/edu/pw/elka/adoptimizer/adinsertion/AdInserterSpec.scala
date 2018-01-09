package pl.edu.pw.elka.adoptimizer.adinsertion

import akka.actor.{ ActorSystem, Props }
import akka.testkit.{ TestActorRef, TestKit, TestProbe }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ BeforeAndAfterAll, Matchers, WordSpec, WordSpecLike }
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class AdInserterSpec extends TestKit(ActorSystem("AdInserterSpec"))
    with WordSpecLike with Matchers with ScalaFutures with BeforeAndAfterAll {

  val parsingMockupActor = TestProbe()
  val classificationEnsembleMockeup = TestProbe()

  /*
  "AdInserter" should {
    "Match paragraphs with results and return paragraph with the highest result" in {
      val samples = List(Sample("text1", "A"), Sample("text2", "A"), Sample("text3", "A"))
      val results = List(0D, 1D, 0.5D)
      val expectedParagraph = "text2"
      val inserterRef = TestActorRef(new AdInserterActor(parsingMockupActor.ref, classificationEnsembleMockeup.ref))
      val inserter = inserterRef.underlyingActor

      val returnedParagraph = inserter.selectBestParagraph(samples, results)

      returnedParagraph === expectedParagraph
    }

  }
  */
}
