package models

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._

object Communication {

  var project: Project = _

  var out: PushEnumerator[JsValue] = _
  
  def load(fileName: String): JsValue = {
  
    var lines = "Error";
  
    try {
      val source = scala.io.Source.fromFile(fileName)
      lines = source.mkString
      source.close()
    }    
    catch {
      case e: Exception => println("Error in Communication.scala - load(): " + e )  
    }
    
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
    try {
      val out = new OutputStreamWriter(
        new FileOutputStream(fileName), "UTF-8")
      out.write(content)
      out.close
    }
    catch {
      case e: Exception => println("Error in Communication.scala - save(): " + e )  
    }
    
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
  
  def compile(filePath:String): JsValue = {
  	  	
    def getType(severity: Int) = severity match {
      case 1 => "warning"
      case 2 => "error"
      case _ => "ignore"
    }

    var probMessages: String = "Error"
    
    try {    
	     probMessages = project.compile(filePath).map(prob => {
	      "{" +
	      "\"source\":\"" + prob.pos.source.replace("\\", "/") + "\"," +
	      "\"row\":" + prob.pos.line + "," +
	      "\"column\":" + prob.pos.column + "," +
	      "\"text\":\"" + prob.msg.replace("\"", "\\\"").replace("\n", "") + "\"," +
	      "\"type\":\"" + getType(prob.severity) + "\"" +
	      "}"
	    }).mkString("[", ",", "]") 
    }
    catch {
      case e: Exception => println("Error in Communication.scala: " + e )  
    }
    
    JsObject(Seq(
      "type" -> JsString("editor"),
      "command" -> JsString("compile"),
      "filename" -> JsString(filePath),
      "report" -> JsString(probMessages))
    ).as[JsValue]
  }
  
  def complete(filePath:String, cursorRow:Int, cursorColumn:Int): JsValue = {
    val options = project.complete(filePath, cursorRow, cursorColumn)
    val optionsString = options.map(o => {
      "{" +
        "\"kind\":\"" + o.kind + "\"," +
        "\"name\":\"" + o.name.replace("\\", "\\\\") + "\"," +
        "\"fullName\":\"" + o.fullName.replace("\\", "\\\\") + "\"," +
        "\"replaceText\":\"" + o.replaceText.replace("\\", "\\\\") + "\"," +
        "\"cursorPos\":" + o.cursorPos + "," +
        "\"symType\":\"" + o.symType.replace("\\", "\\\\") + "\"" +
      "}"
    }).mkString("[", ",", "]")
      
    JsObject(Seq(
      "type" -> JsString("editor"),
      "command" -> JsString("complete"),
      "filename" -> JsString(filePath),
      "row" -> JsString(cursorRow.toString),
      "column" -> JsString(cursorColumn.toString),
      "options" -> JsString(optionsString))
    ).as[JsValue]
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
        channel.push( compile( fileName ) )
      }
      case "save-and-complete" => {
        val value = (msg \ "value").as[String]
        val row = (msg \ "row").as[Int]
        val column = (msg \ "column").as[Int]
        save(fileName, value)
        channel.push( complete( fileName, row, column ))
      }
      case "compile" => {
        channel.push( compile( fileName ) )
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
