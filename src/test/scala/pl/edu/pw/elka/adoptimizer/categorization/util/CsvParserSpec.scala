package pl.edu.pw.elka.adoptimizer.categorization.util

import org.scalatest.WordSpecLike
import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

class CsvParserSpec extends WordSpecLike {
  "Csv parser" should {
    "correctly parse csv lines" in {
      val csv = List("abc,de,1", "xyz,2")
      assert(CsvParser.parse(csv) == List(Sample("abc,de", "1"), Sample("xyz", "2")))
    }
  }
}
