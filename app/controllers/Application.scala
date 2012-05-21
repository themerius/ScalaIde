package controllers

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._

import models.Communication
import models.Project


object Application extends Controller {

  def index = Action { implicit request =>
  	Communication.project = new Project("projectspace")
    Ok(views.html.index("Ace Editor", new File("projectspace")))
  }

 def webSocket = WebSocket.async[JsValue] { request =>

    val in = Iteratee.foreach[JsValue](Communication.commandHandling)

    val out = Enumerator.pushee[JsValue] {
      pushee => pushee.push(
        JsObject(Seq(
          "type" -> JsString("editor"),
          "command" -> JsString("load"),
          "text" -> JsString("Happy Coding!"))
        ).as[JsValue])
      Communication.out = pushee
    }

    models.Terminal.start

    Promise.pure((in,out)) 
  }

}
