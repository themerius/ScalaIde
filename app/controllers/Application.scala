package controllers

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._


object Application extends Controller {

  var out: Enumerator.Pushee[JsValue] = _
  
  def index = Action { implicit request =>
    Ok(views.html.index("Ace Editor", new File("projectspace")))
  }
  
  def load(fileName: String): JsValue = {
    val source = scala.io.Source.fromFile(fileName)
    val lines = source.mkString
    source.close()
    
    JsObject(Seq("command" -> JsString("load"),
    			 "text" -> JsString(lines))).as[JsValue];
    
  }
  
  def loadError = JsObject(Seq("command" -> JsString("error"),
		  					   "error" -> JsString("true"),
    						   "text" -> JsString("Something went wrong while loading!"))).as[JsValue];
    

  def save(fileName: String, content: String) = {
    val out = new OutputStreamWriter(
      new FileOutputStream(fileName), "UTF-8")
    out.write(content)
    out.close
  }
  
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }




  
  /*def webSocket = WebSocket.using[String] { request =>

    val in = Iteratee.foreach[String](this.command).mapDone { _ =>
      println("Disconnected")
    }

    val out = Enumerator.pushee[String] { pushee =>
      pushee.push("The Server welcomes you!\n" +
                  "addressbook.scala$   ===> load the file\n" +
                  "edit the file and safe it with the # key.")
      this.out = pushee
    }

    (in, out)
    
  }*/
  
  def webSocket = WebSocket.async[JsValue] { request =>

    val in = Iteratee.foreach[JsValue](this.commandHandling)

    val out = Enumerator.pushee[JsValue] {
    	pushee => pushee.push(JsObject(Seq("command" -> JsString("load"),
    								 "text" -> JsString("Happy Coding!"))).as[JsValue])
    	this.out = pushee
    }

    Promise.pure((in,out)) 
  }

  /*****
   * 
   * TODO: 1. JSON Commands von Frontend zu Backend:
   *    WebSocket.async[JsValue] { request => ...
    
	    Iteratee.foreach[JsValue]....
	  
	    JSON Objekt generieren: JsObject(Seq("text" -> JsString("test"))).as[JsValue]
   * 
   *    Beispiel:
   *    var jsonExample = {
   *       command: "save",
   *       file: "filename",
   *       content: "filecontent"
   *    }
   * 
   * 
   * 2. Mapping in eine model class auslagern
   * 
   */
  
  def commandHandling(msg: JsValue) = {
    
      //TODO: ERROR when js key not exists
	  val command = (msg \ "command").as[String]
	  val fileName = (msg \ "file").as[String]
	 
	  command match {
	  		case "load" => out.push(load( fileName ))
	  		case "" => out.push(loadError)
	  }
	 
  }

}
