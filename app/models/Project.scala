package models

import java.io.File
import scala.collection.JavaConversions._
import PresentationCompiler.SourceFile
import java.io.{File => JFile}
import play.api.Play

import play.api.libs.json._
import play.api.libs.concurrent._
import play.api.Play.current
import akka.actor._
import akka.util.duration._
import akka.util.Timeout
import akka.pattern.ask

import java.io.{OutputStreamWriter, FileOutputStream, File}

class CompileRobot (projectPath:String, project:ActorRef) {
  
  val scheduler = Akka.system.scheduler.schedule(
      1500 milliseconds,
      1500 milliseconds,
      project,
      CompileAll()
    )
    
  def stop {
    scheduler.cancel()
  }
}

/** Factory for creating a [[models.Project]] actor. */
object Project {   

  var users = Map.empty[String, Tuple2[ActorRef, CompileRobot]]
  
  def join(id: String, projectPath: String) = {
  
    val actor = Akka.system.actorOf(Props(new Project(id, projectPath)));
    val compileRobot = new CompileRobot(projectPath, actor)
    val tuple = (actor, compileRobot)
    
    users = users + (id -> tuple)
  }
  
   def leave(id:String) {
     
    // Stop compileRobot
    users.getOrElse(id, null)._2.stop
    
    users = users - id
  }
  
  def loadError(id:String) = {
    users.getOrElse(id, null)._1 ! LoadError()
  }
  
  def load(id: String, fileName: String) = {
    users.getOrElse(id, null)._1 ! Load(fileName)
  }
  
  def save(id: String, fileName: String, content: String) = {
    users.getOrElse(id,null)._1 ! Save(fileName, content)
  }
    
  def delete(id: String, file: File, msg: JsValue) : Unit = {
    users.getOrElse(id,null)._1 ! Delete(file, msg)
  }
 
  def create(id: String, fileName: String, isDir: Boolean) = {
    users.getOrElse(id,null)._1 ! Create(fileName, isDir)
  }

  def rename(id: String, fileName: String, newFileName: String) = {
    users.getOrElse(id,null)._1 ! Rename(fileName, newFileName) 
  }  
  
  //update the compiler with new or removed files
  def update(id: String) = {
    users.getOrElse(id,null)._1 ! Update() 
  }
  
  //this is for compiling
  def compile(id: String, filePath: String) = {
    users.getOrElse(id,null)._1 ! Compile(filePath)
  }
  
  //this is for auto-completing
  def complete(id: String, filePath: String, line: Int, column: Int) = {
    users.getOrElse(id,null)._1 ! Complete(filePath, line, column)
  }
}

/** A project which scans the project path for jar's and initiates compiling.
  *
  * @constructor create a new project with projectPath.
  * @param id the unique user id.
  * @param projectPath the absolut unix or windows path to the project dir.
  */
class Project(id: String, projectPath: String) extends Actor {
  var compiledJSON : JsValue = _

  def srcDirs = Seq(new File(projectPath))
      
  def sourceFiles = {
    sourceFileMap.clear

    srcDirs.map(srcDir => {
      scanCompilableFiles(srcDir).map(src => {
        val sourceFile = new PresentationCompiler.SourceFile(srcDir, src)
        sourceFileMap += (src -> sourceFile)
        sourceFile
      })
    }).flatten.toSeq
  }

  val compiler = {
    
    def libDirs = {
        val playLibs = Play.current.configuration.getString("framework.directory").get + "/framework/sbt"
        val sbtProj = new SbtProject(projectPath)
        if (sbtProj.isExistent)
         (sbtProj.update).waitFor
        else {
          sbtProj.doBootstrap
          (sbtProj.update).waitFor
        }
        
        val userLibs = projectPath + "/lib_managed"
                
        var allLibs = scanFiles(new File(playLibs), "^[^.].*[.](jar)$".r )
        
        for (file <- scanFiles(new File(userLibs), "^[^.].*[.](jar)$".r ))
          allLibs = allLibs :+ file
          
        (allLibs)
    }    
    new ScalaPresentationCompiler(sourceFiles, libDirs)
  }
       
  // Map of original java.io.File => its representation as a PresentationCompiler.SourceFile
  lazy val sourceFileMap = new scala.collection.mutable.HashMap[File, SourceFile]

  //
  // Get a list of all files in the given directory, with file name filtered by
  // regex. The recurse function indicates whether to recurse into a directory
  // or not.
  //
  def scanFiles(dir: File, regex: scala.util.matching.Regex, recurse: (File) => Boolean = {_=>true}): Seq[File] = {
    if(dir.canRead && dir.isDirectory && recurse(dir)) {
        dir.listFiles.toSeq.collect({
            case f if f.isFile && regex.unapplySeq(f.getName).isDefined => Seq(f)
            case f if f.isDirectory => scanFiles(f, regex)
        }).flatten
    } else {
        Nil
    }
  }
  
  def scanCompilableFiles(dir: File) = scanFiles(dir, "^[^.].*[.](scala|java)$".r)
     
  def receive = {
    case Load(fileName) => {
      var lines = "Error";
    
      try {
        val source = scala.io.Source.fromFile(fileName)
        lines = source.mkString
        source.close()
      }    
      catch {
        case e: Exception => println("Error in Communication.scala - load(): " + e )  
      }
      
      Websocket.send(id, 
        JsObject(Seq(
            "type" -> JsString("editor"),
            "command" -> JsString("load"),
            "filename" -> JsString(fileName),
            "text" -> JsString(lines))
        ).as[JsValue]);
    }
    
    case LoadError() => {
      Websocket.send(id, 
        JsObject(Seq(
          "type" -> JsString("editor"),
          "command" -> JsString("error"),
          "text" -> JsString("Something went wrong while loading!"))
        ).as[JsValue])
    }
    
    case Save(fileName, content) => {
      try {      
        val out = new OutputStreamWriter(
          new FileOutputStream(fileName), "UTF-8")
        out.write(content)
        out.close
      }
      catch {
        case x => println("Error in save: " + x)  
      }
    }
    
    case Delete(file, msg) => {
      if(file.isDirectory){
        val subfiles = file.listFiles
        if(subfiles != null)
          subfiles.foreach{ f => Project.delete(id, f, msg) }
      }
      file.delete
      
      Websocket.send(id, 
        JsObject( Seq(
            "type" -> JsString("editor"),
            "command" -> JsString("remove"),
            "value" -> (msg \ "list"))
            ).as[JsValue])
    }
    
    case Create(fileName, isDir) => {
      val file = new File(fileName);
      if (isDir){
        file.mkdir()
      }
      else{
        file.createNewFile()
      }
    }
    
    case Rename(fileName, newFileName) => {
      val src = new File(fileName)
      val dest = new File(newFileName) 
      src.renameTo(dest);  
    }  
    
    case Update() => {
      compiler.loadSources(sourceFiles)
    }
    
    case Compile(filePath) =>{
      try {        
        def getType(severity: Int) = severity match {
          case 1 => "warning"
          case 2 => "error"
          case _ => "ignore"
        }

        var probMessages = { 
            compiler.loadSources(sourceFiles)
            sourceFileMap.get(new File(filePath)).map(compiler.compile).getOrElse(Seq())
          }.map(prob => {
          "{" +
          "\"source\":\"" + prob.pos.source.replace("\\", "/") + "\"," +
          "\"row\":" + prob.pos.line + "," +
          "\"column\":" + prob.pos.column + "," +
          "\"text\":\"" + prob.msg.replace("\"", "\\\"").replace("\n", "") + "\"," +
          "\"type\":\"" + getType(prob.severity) + "\"" +
          "}"
        }).mkString("[", ",", "]") 
         
        val compiledJSONnew = JsObject(Seq(
            "type" -> JsString("editor"),
            "command" -> JsString("compile"),
            "filename" -> JsString(filePath),
            "report" -> JsString(probMessages))
            ).as[JsValue]
            
           Websocket.send(id, compiledJSONnew)
        }
      } catch {
        case x => println ("Error in compile! " + x)
      }
    }
    
    case CompileAll() =>{
      if (sourceFileMap.size != 0) {
        val filePath = sourceFileMap.keys.toList(0).getPath      
        Project.compile(id, filePath)
      } else {
          compiler.loadSources(sourceFiles)
      }
    }
       
    case Complete(filePath, line, column) => {
      try {
        val options = {
           compiler.loadSources(sourceFiles)
           sourceFileMap.get(new File(filePath)).map(compiler.complete(_, line, column)).getOrElse(Seq())
        }
       
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
        
        Websocket.send(id, 
          JsObject(Seq(
            "type" -> JsString("editor"),
            "command" -> JsString("complete"),
            "filename" -> JsString(filePath),
            "row" -> JsString(line.toString),
            "column" -> JsString(column.toString),
            "options" -> JsString(optionsString))
          ).as[JsValue])
      } catch {
        case x => println("Error in complete: " + x)
        }
    }
  }
}

case class Initialize(username: String, projectPath: String)
case class Load(fileName: String)
case class LoadError()
case class Save(fileName: String, content: String)
case class Delete(file: File, msg: JsValue) 
case class Create(fileName: String, isDir: Boolean)
case class Rename(fileName: String, newFileName: String)
case class Update()
case class Compile(filePath: String)
case class CompileAll()
case class Complete(filePath: String, line: Int, column: Int)

/** Make projects to sbt projects and let sbt update the project (fetch jars).
  *
  * @constructor create a new SbtProject with path.
  * @param path the absolut unix or windows path to the project dir.
  */
class SbtProject(val path: String) {

  val sbtPath = {
    if (System.getProperty("os.name").startsWith("Windows"))
      Play.current.configuration.getString("sbt.windows.path").get
    else
      "sbt"
  }
  
  def isExistent: Boolean = (new File(path + "/build.sbt")).exists

  def buildSbtContent: String = {
    "name := \"ScalaIde Default Project\"\n\n" +
    "version := \"0.1\"\n\n" +
    "scalaVersion := \"2.9.1\"\n\n" +
    "libraryDependencies += \"org.specs2\" %%" +
      "\"specs2\" % \"1.11\" % \"test\"\n\n" +
    "retrieveManaged := true"
  }

  def doBootstrap: Boolean = {
    val file = new java.io.BufferedWriter(
      new java.io.FileWriter(path + "/build.sbt")
    )
    try {
      file.write(buildSbtContent)
      file.close()
      return true
    } catch {
      case e: Exception => return false
    }
  }

  def update = {
    val program = new java.util.ArrayList[String]()
    program.add(this.sbtPath)
    program.add("update")  // Argument
    val javaProcess = new java.lang.ProcessBuilder(program)
    javaProcess.directory(new File(path))
    javaProcess.start()
  }
}
