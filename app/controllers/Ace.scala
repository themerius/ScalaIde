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
  
  def load(fileName : String) = Action {
	val source = scala.io.Source.fromFile(fileName)
	val lines = source .mkString
	source.close ()
	Ok(views.html.ace("Ace", lines))
	}	
}
