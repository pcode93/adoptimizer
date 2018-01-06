package pl.edu.pw.elka.adoptimizer.categorization.util

import java.nio.charset.CodingErrorAction

import scala.io.{ Codec, Source }

object FileReader {
  implicit val codec = Codec("UTF-8")
  codec.onMalformedInput(CodingErrorAction.IGNORE)
  codec.onUnmappableCharacter(CodingErrorAction.IGNORE)

  def fromPath(path: String): List[String] = Source.fromFile(path).getLines().toList
}
