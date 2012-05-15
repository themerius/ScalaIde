package models

import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.io._
import java.io.{File => JFile}
import scala.tools.nsc.interactive.{Global, Response}
import scala.tools.nsc.Settings
import scala.tools.nsc.util.{BatchSourceFile, SourceFile => NSCSourceFile}
import PresentationCompiler._


class ScalaPresentationCompiler(val srcs: Seq[SourceFile], val jars: Seq[JFile])
    extends PresentationCompiler {
  
  import ScalaPresentationCompiler._
  
  val reporter = new PresentationReporter()
  
  val compiler = {
    val sep = JFile.pathSeparator
    
    // TODO: Make classpath modifiable. If I modify it do I need to tell the
    // existing compiler or create a new one?
    val settings = new Settings()
    settings.classpath.value = jars.map(_.getAbsolutePath).mkString("", sep, "")

    // TODO: What does this do?
    /*
    settings.sourcepath.value = {
      List(appRoot).mkString("", sep, "")
    }*/
    
    val global = new Global(settings, reporter)
    
    reporter.compiler = global
    global
  }
  
  // Load the initial set of source files into the compiler
  loadSources(srcs)
  
  override def loadSources(srcFiles: Seq[SourceFile]) = {
  	
    val (updated, deleted) = updateSources(srcFiles)
    
    // Remove the source files that have been deleted
    deleted.map(src => {
      // TODO: Should I instead call askFilesDeleted?
      compiler.removeUnitOf(toSourceFile(src))
    })
    
    // Reload the source files that need to be updated
    val srcList = updated.map(toSourceFile(_)).toList
    //println(srcList)
    val reloadResult = new Response[Unit]
    compiler.askReload(srcList, reloadResult)
    reloadResult.get
  }
  
  
  override def compile(src: SourceFile): Seq[Problem] = {
    val file = toSourceFile(src)
    val typedResult = new Response[compiler.Tree]
    reporter.reset
    compiler.askType(file, false, typedResult)
    typedResult.get
    reporter.problems
  }
  
  override def complete(src: SourceFile, line: Int, column: Int): Seq[CompleteOption] = {
    val sourceFile = toSourceFile(src)
    val completeResult = new Response[List[compiler.Member]]
    val typedResult = new Response[compiler.Tree]
    
    // We have to ask type before asking for type completion
    // TODO: Do I need to ask type on entire file or is it better to ask type
    // against the sub-tree containing the position?
    compiler.askType(sourceFile, false, typedResult)
    typedResult.get
    compiler.askTypeCompletion(sourceFile.position(line, column), completeResult);
    
    val options = completeResult.get match {
      case Left(optionList) => optionList
        //.filter(_.getClass.equals(classOf[compiler.TypeMember]))
        //.filter(_.sym.decodedName.matches("^[a-zA-Z_].*"))
        .map(option => {
          println(option)
          val typeMember = option.asInstanceOf[compiler.TypeMember]
          /*
          println(typeMember.sym)
          println(typeMember.tpe)
          println(typeMember.asInstanceOf[compiler.TypeMember])
          println(typeMember.accessible)
          println(typeMember.inherited)
          println(typeMember.viaView)
          println("=====================")
          println(option.sym)
          println(option.sym.kindString)
          println(option.sym.simpleName)
          println(option.sym.fullName)
          println(option.sym.encodedName)
          println(option.sym.decodedName)
          println(option.sym.infoString(option.tpe))
          println(option.sym.infosString)
          println("=====================")
          */
          
          // TODO: How do I do this using pattern matching?
          var replaceText = option.sym.decodedName.toString
          var cursorPos = replaceText.length
          if(option.tpe.isInstanceOf[compiler.MethodType]) {
            val methodType = option.tpe.asInstanceOf[compiler.MethodType]
            if(methodType.params.nonEmpty) {
              replaceText += "()"
              cursorPos = replaceText.length - 1
            }
          }
          
          CompleteOption(option.sym.kindString, option.sym.decodedName.toString, option.sym.fullName,
              replaceText, cursorPos, option.sym.infoString(option.tpe))
          
        }).sortWith((o1, o2) => (o1.name < o2.name))
      case _ => List[CompleteOption]()
    }
    
    options.toSeq
  }
  
  
  class PresentationReporter extends Reporter {
  	  	
    import PresentationReporter._
    import scala.collection.mutable.ListBuffer
    
    var compiler: Global = null
    var problems = ListBuffer[Problem]()
        
    override def info0(pos: scala.tools.nsc.util.Position, msg: String, severity: Severity, force: Boolean): Unit = {
      severity.count += 1

      try {

        if(pos.isDefined) {
          //val source = pos.source
          //val length = source.identifier(pos, compiler).map(_.length).getOrElse(0)
          val position = PresentationCompiler.Position(pos.source.path, pos.line, pos.column)
          
          
          //SISCHNEE: WRITE AN ERROR AND LOOK AT CONSOLE:
          println(formatMessage(msg))
          
          
          problems += Problem(position, formatMessage(msg), severity.id)
        }
      } catch {
        case ex : UnsupportedOperationException => 
      }
    }
    
    override def reset {
      super.reset
      problems.clear
    }
  }
  
  object PresentationReporter {
    def formatMessage(msg: String) = {
      msg.map{
        case '\n' => ' '
        case '\r' => ' '
        case c => c
      }.mkString("","","")
    }
  }
}

object ScalaPresentationCompiler {
  def toSourceFile(file: PresentationCompiler.SourceFile): NSCSourceFile = {
    new BatchSourceFile(new PlainFile(file.src))
  }
}
