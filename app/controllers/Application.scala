package controllers

import java.io.{OutputStreamWriter, FileOutputStream, File}
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._

import models._
import views._

import scala.util.Random


object Application extends Controller with Secured {

  def index = IsAuthenticated { username => implicit request =>
    User.findByEmail(username).map { user =>
    Communication.project = new Project(user.path)
    Ok(html.index("ScalaIDE", new File("projectspaces/" + user.path), user.id + "", user.name)) 
    }.getOrElse(Forbidden)
  }
    
  def webSocket(id:String) = WebSocket.async[JsValue] { request  =>
    Websocket.join(id)
  }
}
