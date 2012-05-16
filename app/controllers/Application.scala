package controllers

import java.io.{OutputStreamWriter, FileOutputStream, File}
import play.api._
import play.api.mvc._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import models.Websocket
import scala.util.Random


object Application extends Controller {

  def index = Action { implicit request =>
    
    // TODO Random needs to be replaced with some sort of session id.
  	var random = new Random().nextInt().toString()
  	println(random)
  	
    Ok(views.html.index("Ace Editor", new File("projectspace"), random))
  }

  
  def webSocket(id:String) = WebSocket.async[JsValue] { request  =>
  	
    models.Terminal.start
    
    Websocket.join(id)
  }
}
