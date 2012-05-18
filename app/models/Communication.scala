package models

import java.io.{OutputStreamWriter, FileOutputStream, File}

import play.api._
import play.api.mvc._

import play.api.libs.json._
import play.api.libs.iteratee._

import play.api.libs.concurrent._

object Communication {

  var out: Enumerator.Pushee[JsValue] = _
  
  var project: Project = _
 
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
  
  def compile(filePath:String): JsValue = {
  	  	
    def getType(severity: Int) = severity match {
      case 1 => "warning"
      case 2 => "error"
      case _ => "ignore"
    }
            
    //SISCHNEE: TODO: problem listbuffer is empty?!
    var probMessages: String = project.compile(filePath).map(prob => {
      "{" +
      "\"source\":\"" + prob.pos.source.replace("\\", "/") + "\"," +
      "\"row\":" + prob.pos.line + "," +
      "\"column\":" + prob.pos.column + "," +
      "\"text\":\"" + prob.msg.replace("\"", "\\\"").replace("\n", "") + "\"," +
      "\"type\":\"" + getType(prob.severity) + "\"" +
      "}"
    }).mkString("[", ",", "]")
    
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

  def commandHandling(msg: JsValue) = {
    //TODO: ERROR when js key not exists or IO Exception
    val command = (msg \ "command").as[String]
    val fileName = (msg \ "file").as[String]

    command match {
      case "load" => out.push(load( fileName ))
      case "save" => {
        val value = (msg \ "value").as[String]
        save(fileName, value)
        out.push( compile( fileName ) )
      }
      case "save-and-complete" => {
        val value = (msg \ "value").as[String]
        val row = (msg \ "row").as[Int]
        val column = (msg \ "column").as[Int]
        save(fileName, value)
        out.push( complete( fileName, row, column ))
      }
      case "command" => {  // Terminal command!
        val cmd = (msg \ "value").as[String]
        out.push( Terminal.sendCommand(cmd) )
      }
      case "compile" => {
        out.push( compile( fileName ) )
      }
      case "create" => {
      	val folder = (msg \ "folder").as[Boolean]
        create(fileName, folder)
        if ( !folder )
          out.push(load( fileName ))
      }
      case "remove" => {
        delete(new File(fileName))
        out.push( JsObject( Seq(
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
          out.push(load( fileName ))
      }
      case "" => out.push(loadError)
    }
  }

}
