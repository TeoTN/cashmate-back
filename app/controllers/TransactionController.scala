package controllers

import com.google.inject._
import daos.{AccountDAO, CouponDAO, TransactionDAO}
import models.Transaction
import play.Logger
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class TransactionController @Inject()(accountDAO: AccountDAO)(transactionDAO: TransactionDAO)(couponDAO: CouponDAO)
  extends Controller {

  def createTransaction(couponId: Long, token: String) = Action.async { implicit request =>
    Logger.debug("CREATE " + request.body.toString)
    forAuthorizedUser(token, (accountId) => {
      accountDAO.findById(accountId) flatMap {
        case None => Future.successful(BadRequest(Json.toJson(errorJson("User does not exist"))))
        case Some(account) => couponDAO.findById(couponId) flatMap {
          case None =>
            Future.successful(BadRequest(Json.toJson(errorJson("User does not exist"))))
          case Some(coupon) =>
            if (account.points > coupon.points) {
              transactionDAO.insert(new Transaction(couponId, accountId)) map {
                transaction => Ok(Json.toJson(transaction))
              }
            } else {
              Future.successful(BadRequest(Json.toJson(errorJson("Insufficient points"))))
            }
        }
      }
    })
  }

  def checkTransaction(transactionId: Long, token: String) = Action.async { implicit request =>
    Logger.debug("CHECK " + request.body.toString)
    forAuthorizedUser(token, (accountId) => {
      transactionDAO.findById(transactionId) map {
        case None =>
          BadRequest(Json.toJson(errorJson("No such transaction")))
        case Some(transaction) =>
          if (transaction.accountId == accountId) {
            Ok(Json.toJson(transaction))
          } else {
            BadRequest(Json.toJson(errorJson("User not related")))
          }
      }
    })
  }

  def acceptTransactionByClient(transactionId: Long, token: String) = Action.async { implicit request =>
    Logger.debug("CLIENT CONFIRM " + request.body.toString)
    forAuthorizedUser(token, (accountId) => {
      transactionDAO.findById(transactionId) flatMap {
        case None =>
          Future.successful(BadRequest(Json.toJson(errorJson("No such transaction"))))
        case Some(transaction) =>
          if (transaction.accountId == accountId) {
            couponDAO.findById(transaction.couponId) map {
              case None =>
                BadRequest(Json.toJson(errorJson("No coupon found")))
              case Some(coupon) =>
                accountDAO.removePoints(accountId, coupon.points)
                transactionDAO.deleteById(transaction.id.get)
                Ok(Json.toJson(okJson))
            }
          } else {
            Future.successful(BadRequest(Json.toJson(errorJson("User not related"))))
          }
      }
    })
  }

  def acceptTransactionByVendor(code: Int) = Action.async { implicit request =>
    Logger.debug("VENDOR CONFIRM " + request.body.toString)
    transactionDAO.findByCode(code) flatMap {
      case None => Future.successful(BadRequest(Json.toJson(errorJson("No such transaction"))))
      case Some(transaction) => transactionDAO.acceptTransaction(transaction.id.get) map {
        out => Ok(Json.toJson(okJson))
      }
    }
  }

  private def forAuthorizedUser(token: String, func: Long => Future[Result]) =
    accountDAO.findByToken(token) flatMap {
      case None => Future.successful(Unauthorized(Json.toJson(errorJson("Wrong token"))))
      case Some(account) => func(account.id.get)
    }

  private def errorJson(err: String = "ERR") = Json.obj(
    "answer" -> err
  )

  private def okJson = Json.obj(
    "ok" -> "OK"
  )

}
