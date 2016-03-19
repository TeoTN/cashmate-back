package controllers

import com.google.inject.Inject
import daos.CouponDAO
import play.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

class CouponController @Inject()(couponDAO: CouponDAO) extends Controller {

  def all = Action.async {
    Logger.debug("ALL COUPONS")
    couponDAO.findAll() map {
      out => Ok(Json.toJson(Map("coupons" -> out)))
    }
  }

  def pattern(pattern: String) = Action.async {
    Logger.debug("CUSTOM COUPONS")
    couponDAO.findByPattern(pattern) map {
      out => Ok(Json.toJson(Map("coupons" -> out)))
    }
  }

  def single(id: Long) = Action.async {
    couponDAO.findById(id) map {
      case None => BadRequest("No such coupon")
      case Some(coupon) => Ok(Json.toJson(coupon))
    }
  }

}
