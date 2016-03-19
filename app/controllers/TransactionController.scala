package controllers

import com.google.inject._
import daos.{AccountDAO, CouponDAO, TransactionDAO}
import models.Transaction
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TransactionController @Inject()(accountDAO: AccountDAO)(transactionDAO: TransactionDAO)(couponDAO: CouponDAO)
  extends Controller {

  def createTransaction(couponId: Long, token: String) = Action.async { implicit request =>
    forAuthorizedUser(token, (accountId) => {
      transactionDAO.insert(new Transaction(couponId, accountId)) map {
        transaction => Ok(Json.toJson(transaction))
      }
    })
  }

  def checkTransaction(transactionId: Long, token: String) = Action.async { implicit request =>
    forAuthorizedUser(token, (accountId) => {
      transactionDAO.findById(transactionId) map {
        case None =>
          BadRequest(Json.toJson(errorJson))
        case Some(transaction) =>
          if (transaction.accountId == accountId) {
            Ok(Json.toJson(transaction))
          } else {
            BadRequest(Json.toJson(errorJson))
          }
      }
    })
  }

  def acceptTransactionByClient(transactionId: Long, token: String) = Action.async { implicit request =>
    forAuthorizedUser(token, (accountId) => {
      transactionDAO.findById(transactionId) flatMap {
        case None =>
          Future.successful(BadRequest(Json.toJson(errorJson)))
        case Some(transaction) =>
          if (transaction.accountId == accountId) {
            couponDAO.findById(transaction.couponId) map {
              case None =>
                BadRequest(Json.toJson(errorJson))
              case Some(coupon) =>
                accountDAO.removePoints(accountId, coupon.points)
                transactionDAO.deleteById(transaction.id.get)
                Ok(Json.toJson(okJson))
            }
          } else {
            Future.successful(BadRequest(Json.toJson(errorJson)))
          }
      }
    })
  }

  def acceptTransactionByVendor(code: Int) = Action.async { implicit request =>
    transactionDAO.findByCode(code) flatMap {
      case None => Future.successful(BadRequest(Json.toJson(errorJson)))
      case Some(transaction) => transactionDAO.acceptTransaction(transaction.id.get) map {
        out => Ok(Json.toJson(okJson))
      }
    }
  }

  private def forAuthorizedUser(token: String, func: Long => Future[Result]) =
    accountDAO.findByToken(token) flatMap {
      case None => Future.successful(Unauthorized(Json.toJson(errorJson)))
      case Some(account) => func(account.id.get)
    }

  private def errorJson = Json.obj(
    "answer" -> "ERR"
  )

  private def okJson = Json.obj(
    "ok" -> "OK"
  )

}
