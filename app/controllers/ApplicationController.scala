package controllers

import play.api.mvc.{Action, Controller}

class ApplicationController extends Controller {

  def index = Action {
    Ok("GET")
  }

  def index2 = Action {
    Ok("PUT")
  }

}
