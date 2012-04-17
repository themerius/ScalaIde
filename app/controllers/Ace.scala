package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._


object Ace extends Controller {

  var out: Enumerator.Pushee[String] = _

  def index = Action { implicit request =>
    Ok(views.html.ace("Ace Editor"))
  }
  
  def load(fileName : String): String = {
    val source = scala.io.Source.fromFile(fileName)
    val lines = source.mkString
    source.close()
    lines
  }


  def aceSocket = WebSocket.using[String] { request =>

    val in = Iteratee.foreach[String](this.myMsg).mapDone { _ =>
      println("Disconnected")
    }

    val out = Enumerator.pushee[String] { pushee =>
      pushee.push("The Server welcomes you!")
      this.out = pushee
    }

    (in, out)
    
  }
  
  def myMsg(msg: String) = msg match { // event ausloesen

    case "bla" => println("You typed bla.")
    case "bli" => out.push("hahha")
    case "open addressbook.scala" => out.push(load("addressbook.scala"))
    case msg => println(msg)
  }
  
}
