package pl.edu.pw.elka.adoptimizer.http

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import pl.edu.pw.elka.adoptimizer.domain.Ad
import pl.edu.pw.elka.adoptimizer.http.routes.AdForWebsite
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport {
  import DefaultJsonProtocol._

  implicit val adJsonFormat = jsonFormat2(Ad)
  implicit val adForWebsiteJsonFormat = jsonFormat2(AdForWebsite)
}
