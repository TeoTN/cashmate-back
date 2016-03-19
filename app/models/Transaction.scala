package models

import play.api.libs.json.Json

import scala.util.Random

case class Transaction(id: Option[Long], code: Int, timestamp: Long, couponId: Long, accountId: Long, acceptedByVendor: Boolean) {

  def this(couponId: Long, accountId: Long) =
    this(None, 100000 + new Random().nextInt(900000), System.currentTimeMillis(), couponId, accountId, false)

}

object Transaction {

  implicit val authenticationRequestFormat = Json.format[Transaction]

}