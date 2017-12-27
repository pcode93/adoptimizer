package pl.edu.pw.elka.adoptimizer.categorization.util

import org.scalatest.WordSpecLike
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class DatasetUtilsSpec extends WordSpecLike {
  val dataset = List(Sample("1", "A"), Sample("2", "A"), Sample("3", "B"), Sample("4", "B"))

  "Dataset utils" should {
    "create folds for dataset" in {
      val expectedFolds = List(
        List(Sample("1", "A")), List(Sample("2", "A")),
        List(Sample("3", "B")), List(Sample("4", "B"))
      ).reverse

      assert(DatasetUtils.createFolds(dataset, 4) == expectedFolds)
    }
  }
}
