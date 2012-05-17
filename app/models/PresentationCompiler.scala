package models

import scala.tools.nsc.util._
import scala.tools.nsc.reporters.Reporter
import scala.tools.nsc.io._
import java.io.{File => JFile}
import scala.tools.nsc.interactive.{Global, Response}
import scala.tools.nsc.Settings


trait PresentationCompiler {
  import PresentationCompiler._
  
  def compile(src: SourceFile): Seq[Problem] = Seq()
  
  def complete(src: SourceFile, line: Int, column: Int): Seq[CompleteOption] = Seq()
  
  // Loads the given set of source files into the compiler
  def loadSources(srcFiles: Seq[SourceFile])
  
  
  // Keeps track of the files that are currently loaded into the compiler,
  // and their last modified date
  val loadedSrcs = scala.collection.mutable.Map[SourceFile, Long]()
  
  /**
   * Compare the list of files to the existing list. Updates the existing list,
   * and figures out which files were updated and which deleted.
   * @return (updated, deleted)
   */
  def updateSources(srcFiles: Seq[SourceFile]): (Seq[SourceFile], Seq[SourceFile]) = {
    import scala.collection.mutable.ListBuffer;
    
    val updated = new ListBuffer[SourceFile]
    
    // For each source file, if it's already in the map, check its last
    // modified date to see if it needs to be updated. If it's not in the
    // map, add it.
    srcFiles.foreach(src => {
      loadedSrcs.get(src) match {
        case Some(lastModified) if(src.src.lastModified > lastModified) => updated += src
        case None => {
          updated += src
          loadedSrcs.put(src, src.src.lastModified)
        }
        case _ =>
      }
    })
    
    // For each file in the map, if it's not in the list of source files,
    // remove it from the map and compiler
    val deleted = new ListBuffer[SourceFile]
    loadedSrcs.keySet.filter(!srcFiles.contains(_)).foreach(src => {
      loadedSrcs.remove(src)
      deleted += src
    })
        
    (updated, deleted)
  }

}

object PresentationCompiler {
  case class Position(source: String, line: Int, column: Int)
  case class Problem(pos: PresentationCompiler.Position, msg: String, severity: Int)
  case class CompleteOption(kind: String, name: String, fullName: String, replaceText: String, cursorPos: Int, symType: String)
  case class SourceFile(srcDir: JFile, src: JFile)
}
