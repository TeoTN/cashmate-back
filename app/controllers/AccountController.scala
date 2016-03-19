package controllers

import javax.inject._

import daos.AccountDAO
import models.Account
import org.mindrot.jbcrypt.BCrypt
import play.Logger
import play.api.mvc._
import play.api.libs.json._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class AccountController @Inject()(accountDAO: AccountDAO) extends Controller {

  def login = Action.async(BodyParsers.parse.json) { implicit request =>
    implicit val authenticationRequestFormat = Json.format[AuthenticationRequest]
    Logger.debug(request.body.toString())
    request.body.validate[AuthenticationRequest].fold(
      error => Future.successful(BadRequest(Json.toJson(errorJson))),
      authenticationRequest =>
        accountDAO.findByLogin(authenticationRequest.login) map {
          case None =>
            BadRequest(Json.toJson(errorJson))
          case Some(account) =>
            if (BCrypt.checkpw(authenticationRequest.password, account.passwordHash)) {
              Ok(Json.toJson(okJson(account)))
            } else {
              Unauthorized(Json.toJson(errorJson))
            }
        }
    )
  }

  def register = Action.async(BodyParsers.parse.json) { implicit request =>
    implicit val registrationRequestFormat = Json.format[RegistrationRequest]
    request.body.validate[RegistrationRequest].fold(
      error => Future.successful(BadRequest(Json.toJson(errorJson))),
      registrationRequest =>
        accountDAO.insert(new Account(
          registrationRequest.login, BCrypt.hashpw(registrationRequest.password, salt), registrationRequest.email
        )) map {
          account => Ok(Json.toJson(okJson(account)))
        }
    )
  }

  def logout = Action {
    Ok("OK")
  }

  private val salt = BCrypt.gensalt()

  private def errorJson = Json.obj(
    "answer" -> "ERR"
  )

  private def okJson(account: Account) = Json.obj(
    "answer" -> "OK",
    "id" -> account.id.get,
    "login" -> account.login,
    "email" -> account.email,
    "points" -> account.points,
    "token" -> account.token
  )

  private case class AuthenticationRequest(login: String, password: String)

  private case class RegistrationRequest(login: String, password: String, email: String)

}
