package controllers

import com.google.inject.Inject
import daos.CouponDAO
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

class CouponController @Inject()(couponDAO: CouponDAO) extends Controller {

  def all = Action.async {
    couponDAO.findAll() map {
      out => Ok(Json.toJson(Map("coupons" -> out)))
    }
  }

}
