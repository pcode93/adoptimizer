package pl.edu.pw.elka.adoptimizer.categorization.preprocessing

import org.scalatest.WordSpecLike

class TextFilterSpec extends WordSpecLike {
  "Text filters" should {
    "Convert whitespaces to single spaces" in {
      val test = "test\n\n\n\r\ttest\t\t\t\r\ntest"
      assert(WhitespaceConvertingFilter().filter(test) == "test test test")
    }

    "Filter all characters except letters" in {
      val test = "test test123&^%$<>: \ntest\"[]{}{}<<<,."
      assert(TextCleaningFilter().filter(test) == "test test \ntest")
    }
  }
}
