package controllers

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._

object Ace extends Controller {

  def index = Action { implicit request =>
    val world= """class HelloWorld {
  def sayHello = "Hello"
}"""
    Ok(views.html.ace("Ace", world))
  }
  
  def load(fileName : String) = Action { implicit request =>
	val source = scala.io.Source.fromFile(fileName)
	val lines = source .mkString
	source.close ()
	Ok(views.html.ace("Ace", lines))
  }
  
  /*def aceSocket = WebSocket.async[JsValue] { request =>
    
	    // Just consume and ignore the input
      val in = Iteratee.foreach[JsValue](x => println(x))
	  
	  // Send a single 'Hello!' message and close
	  val out = Enumerator(JsObject(Seq("text" -> JsString("test"))).as[JsValue])
	  //val out = Enumerator(JsValue)
      
      
      Promise.pure( (in, out) )
    
  }*/
  
  	class Message {
	  var myText:String = ""
	}
	
	object SocketMessage extends Message {
	  def socketText( msg: String ) = { println(msg) }
	}
     
  
   def aceSocket = WebSocket.using[String] { request =>
      
    // Log events to the console
    val in = Iteratee.foreach[String](this.myMsg).mapDone { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator(SocketMessage.myText)

    (in, out)
    
   }
  
   def myMsg(msg: String) = msg match { // hier koennte man events ausloesen!

     case "bla" => println("You typed bla.")
     case msg => SocketMessage.socketText( msg )
  }
  
}
