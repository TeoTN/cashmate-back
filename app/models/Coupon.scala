package models

case class Coupon(id: Option[Long], location: String, imageUrl: String, title: String, points: Int, vendor: String)