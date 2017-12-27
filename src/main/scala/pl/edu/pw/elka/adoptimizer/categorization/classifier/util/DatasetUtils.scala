package pl.edu.pw.elka.adoptimizer.categorization.classifier.util

import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

object DatasetUtils {
  def createFolds(dataset: List[Sample], k: Int): List[List[Sample]] = {
    val split = dataset.length / k

    def it(data: List[Sample], folds: List[List[Sample]] = List(), foldNum: Int = 1): List[List[Sample]] = {
      val nextFold = data.splitAt(split)
      if (foldNum == k - 1) nextFold._2 :: (nextFold._1 :: folds)
      else it(nextFold._2, nextFold._1 :: folds, foldNum + 1)
    }

    it(dataset)
  }

  def crossValidate(dataset: List[Sample], k: Int)(trainFunc: (List[Sample], List[Sample]) => Double): Double = {
    val folds = createFolds(dataset, k).zipWithIndex
    folds.foldLeft(0D)((acc, valid) => acc + trainFunc(folds.filter(_._2 != valid._2).flatMap(_._1), valid._1)) / k
  }
}
