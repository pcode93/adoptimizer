package classifier

/**
  * Created by leszek on 26/12/2017.
  */
abstract class KnowledgeBase {
  def train(samples: Array[Sample])
}
