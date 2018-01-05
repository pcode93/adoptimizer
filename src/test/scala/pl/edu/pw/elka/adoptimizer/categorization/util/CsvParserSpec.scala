package pl.edu.pw.elka.adoptimizer.categorization.util

import org.scalatest.WordSpecLike
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class CsvParserSpec extends WordSpecLike {
  "Csv parser" should {
    "correctly parse csv lines" in {
      val csv = List("content1,category1", "content2,category2")
      assert(CsvParser.parse(csv) == List(Sample("content1", "category1"), Sample("content2", "category2")))
    }
  }
}
