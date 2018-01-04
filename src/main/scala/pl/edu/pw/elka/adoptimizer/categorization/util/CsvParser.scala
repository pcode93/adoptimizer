package pl.edu.pw.elka.adoptimizer.categorization.util

object CsvParser {
  def parse(lines: List[String], separator: String = ","): List[Map[String, String]] = {
    val columns = lines.head.split(separator)
    lines.tail.map(_.split(separator).zipWithIndex.map(value => columns(value._2) -> value._1).toMap)
  }
}
