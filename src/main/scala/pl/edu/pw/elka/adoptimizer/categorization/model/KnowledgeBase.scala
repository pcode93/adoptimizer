package pl.edu.pw.elka.adoptimizer.categorization.model

/**
 * Created by leszek on 26/12/2017.
 */
trait KnowledgeBase {
  def train(samples: List[Sample])
}
