package models

import play.api.libs.json.Json

case class Coupon(id: Option[Long], location: String, imageUrl: String, title: String, points: Int, vendor: String)

object Coupon {

  implicit val authenticationRequestFormat = Json.format[Coupon]

}