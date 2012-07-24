package models

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._


object Communication {
  
  def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }
  
  def commandHandling(message: JsValue, terminal: models.Terminal, id: String) = {
    val messageType = (message \ "type").as[String]
    val command = (message \ "command").as[String]

    messageType match { 
      case "editor" => editorCommandHandling(message, command, id)
      case "browser" => browserCommandHandling(message, command, id)
      case "terminal" => terminalCommandHandling(message, command, id, terminal)
      case _ => println("Received undefined messages from websocket.")
    }
  }

  def editorCommandHandling(message: JsValue, command: String, id: String) = {
    val msg = message.as[JsObject]
    var fileName = ""
    if ( msg.keys.contains("file") )
      fileName = (msg \ "file").as[String]

    command match {
      case "load" => Project.load(id, fileName)
      case "save" => {
        val value = (msg \ "value").as[String]
        Project.save(id, fileName, value)
      }
      case "save-and-complete" => {
        val value = (msg \ "value").as[String]
        val row = (msg \ "row").as[Int]
        val column = (msg \ "column").as[Int]
        Project.save(id, fileName, value)
        
        Thread.sleep(200)
        Project.complete( id, fileName, row, column )
      }
      case "compile" => {
        Project.compile( id, fileName )
      }
      case "create" => {
        val folder = (msg \ "folder").as[Boolean]
        Project.create(id, fileName, folder)
        if ( !folder )
          Project.load(id, fileName)
      }
      case "remove" => {
        Project.delete(id, new File(fileName), msg)
      }
      case "rename" => {
        val oldFileName = (msg \ "value").as[String]
        val folder = (msg \ "folder").as[Boolean]
        Project.rename(id, oldFileName, fileName )
        if ( !folder )
          Project.load(id, fileName)
      }
      case _ => Project.loadError(id)
    }
  }

  def browserCommandHandling(message: JsValue, command: String, id: String) = {

  }

  def terminalCommandHandling(message: JsValue, command: String, id: String, terminal: models.Terminal) = {
    
    command match {
      case "keyEvent" => {
        val cmd = (message \ "value").as[Int] 
        terminal.handleKey(cmd.toByte)
      }
      case _ => Project.loadError(id)
    }
  }

}
