package pl.edu.pw.elka.adoptimizer.categorization.util

import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

object CsvParser {
  def parse(lines: List[String], separator: String = ","): List[Sample] = {
    lines.map(line => {
      val parts = line.split(separator)
      val content = parts.take(parts.length - 1).mkString(separator)

      Sample(content.replace("\\n", "\n").replace("\\r", "\r"), parts(parts.length - 1))
    })
  }
}
