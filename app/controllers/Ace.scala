package controllers

import play.api._
import play.api.mvc._

object Ace extends Controller {
  
  def index = Action {
    val world= """class HelloWorld {
  def sayHello = "Hello"
}"""
    Ok(views.html.ace("Ace", world))
  }
  
}
