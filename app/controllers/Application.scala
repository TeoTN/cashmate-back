package controllers

import javax.inject._

import daos.AccountDAO
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class Application @Inject()(accountDAO: AccountDAO) extends Controller {

  def index = Action.async {
    accountDAO.findAll() map (out => Ok(out.head.login))
  }

}
