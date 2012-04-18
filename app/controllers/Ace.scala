package controllers

import java.io.{OutputStreamWriter, FileOutputStream}

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
  
  def load(fileName: String): String = {
    val source = scala.io.Source.fromFile(fileName)
    val lines = source.mkString
    source.close()
    lines
  }

  def save(fileName: String, content: String) = {
    val out = new OutputStreamWriter(
      new FileOutputStream(fileName), "UTF-8")
    out.write(content)
    out.close
  }

  def aceSocket = WebSocket.using[String] { request =>

    val in = Iteratee.foreach[String](this.myMsg).mapDone { _ =>
      println("Disconnected")
    }

    val out = Enumerator.pushee[String] { pushee =>
      pushee.push("The Server welcomes you!\n" +
                  "addressbook.scala$   ===> load the file\n" +
                  "edit the file and safe it with the # key.")
      this.out = pushee
    }

    (in, out)
    
  }

  def myMsg(msg: String) = msg match { // event ausloesen
    case "bla" => println("You typed bla.")
    case "bli" => out.push("hahha")
    case msg if msg.endsWith("scala") => out.push( load(msg) )
    case msg => save( msg.split('!')(1), msg.split('!')(2) )
  }

}
