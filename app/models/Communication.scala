package models

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._

object Communication {
 
  var out: PushEnumerator[JsValue] = _
  
  def load(fileName: String): JsValue = {
    val source = scala.io.Source.fromFile(fileName)
    val lines = source.mkString
    source.close()

    JsObject(Seq(
      "type" -> JsString("editor"),
      "command" -> JsString("load"),
      "filename" -> JsString(fileName),
      "text" -> JsString(lines))
    ).as[JsValue];
    
  }

  def loadError = JsObject(Seq(
      "type" -> JsString("editor"),
      "command" -> JsString("error"),
      "text" -> JsString("Something went wrong while loading!"))
    ).as[JsValue];

  def save(fileName: String, content: String) = {
    val out = new OutputStreamWriter(
      new FileOutputStream(fileName), "UTF-8")
    out.write(content)
    out.close
  }
  
  def delete(file: File) : Unit = {
    if(file.isDirectory){
      val subfiles = file.listFiles
      if(subfiles != null)
        subfiles.foreach{ f => delete(f) }
    }
    file.delete
  }
  
  def create(fileName: String, isDir: Boolean) = {
    val file = new File(fileName);
    if (isDir){
      file.mkdir()
    }
    else{
      file.createNewFile()
    }
  }

  def rename(fileName: String, newFileName: String) = {
    val src = new File(fileName)
    val dest = new File(newFileName) 
    src.renameTo(dest);  
  }

  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  def commandHandling(message: JsValue, channel: PushEnumerator[JsValue] ) = {
    if (channel != null)
    {
	    val messageType = (message \ "type").as[String]
	    val command = (message \ "command").as[String]
	
	    messageType match { 
	      case "editor" => editorCommandHandling(message, command, channel)
	      case "browser" => browserCommandHandling(message, command, channel)
	      case "terminal" => terminalCommandHandling(message, command, channel)
	      case _ => println("Received undefined messages from websocket.")
	    }
    }

  }

  def editorCommandHandling(message: JsValue, command: String, channel: PushEnumerator[JsValue]) = {
    val msg = message.as[JsObject]
    var fileName = ""
    if ( msg.keys.contains("file") )
      fileName = (msg \ "file").as[String]

    command match {
      case "load" => channel.push(load( fileName ))
      case "save" => {
        val value = (msg \ "value").as[String]
        save(fileName, value)
      }
      case "create" => {
        val folder = (msg \ "folder").as[Boolean]
        create(fileName, folder)
        if ( !folder )
          channel.push(load( fileName ))
      }
      case "remove" => {
        delete(new File(fileName))
        channel.push( JsObject( Seq(
          "type" -> JsString("editor"),
          "command" -> JsString("remove"),
          "value" -> (msg \ "list"))
          ).as[JsValue]
        )
      }
      case "rename" => {
        val oldFileName = (msg \ "value").as[String]
        val folder = (msg \ "folder").as[Boolean]
        rename( oldFileName, fileName )
        if ( !folder )
          channel.push(load( fileName ))
      }
      case _ => channel.push(loadError)
    }
  }

  def browserCommandHandling(message: JsValue, command: String, channel: PushEnumerator[JsValue]) = {

  }

  def terminalCommandHandling(message: JsValue, command: String, channel: PushEnumerator[JsValue]) = {
    
    out = channel
    
    command match {
      case "keyEvent" => {
        val cmd = (message \ "value").as[Int] 
        Terminal.handleKey(cmd.toByte)
      }
      case _ => channel.push(loadError)
    }
  }

}
