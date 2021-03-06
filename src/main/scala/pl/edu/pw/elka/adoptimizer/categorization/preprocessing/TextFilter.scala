package pl.edu.pw.elka.adoptimizer.categorization.preprocessing

object TextFilter {
  val complex = ComplexTextFilter(LowerCaseFilter(), WhitespaceConvertingFilter(), TextCleaningFilter())
}

trait TextFilter extends Serializable {
  def filter(text: String): String
}

case class ComplexTextFilter(private val filters: TextFilter*) extends TextFilter {
  override def filter(text: String): String = filters.foldLeft(text)((text, filter) => filter.filter(text))
}

case class WhitespaceConvertingFilter() extends TextFilter {
  override def filter(text: String): String = text.replaceAll("[\n\r\t]+", " ")
}

case class TextCleaningFilter() extends TextFilter {
  override def filter(text: String): String = text.replaceAll("[^a-zA-Z\\s]", "")
}

case class LowerCaseFilter() extends TextFilter {
  override def filter(text: String): String = text.toLowerCase
}
