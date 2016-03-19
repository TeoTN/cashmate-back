package controllers

import play.api.Logger
import play.api.mvc.{Action, Controller}

class ApplicationController extends Controller {

  def index = Action {
    Logger.info("connected")
    Ok("DUPA BEZ CYCOW")
  }

  def index2 = Action {
    Logger.info("connected2")
    Ok("DUPA I CYCE")
  }

}
