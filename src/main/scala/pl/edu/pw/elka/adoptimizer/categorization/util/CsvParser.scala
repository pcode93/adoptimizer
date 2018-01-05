package pl.edu.pw.elka.adoptimizer.categorization.util

import pl.edu.pw.elka.adoptimizer.categorization.model.Sample

object CsvParser {
  def parse(lines: List[String], separator: String = ","): List[Sample] =
    lines.map(_.split(separator)).map(features => Sample(features(0), features(1)))
}
