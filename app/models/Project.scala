package models

import java.io.File
import scala.collection.JavaConversions._

import PresentationCompiler.SourceFile

import java.io.{File => JFile}


class Project(projectPath: String) {   

  val root = new File(projectPath)
  
  def srcDirs = Seq(root)
  
  val compiler = {
  
  	//SISCHNEE: TODO: not hardcoded
   // lazy val playRoot = "D:\\play"
    
    def libDirs = {
     // val playLibs = playRoot + "/framework/sbt/"
     // val scalaLibs = "D:\\eclipse/configuration/org.eclipse.osgi/bundles/768/1/.cp/lib"
    	val scalaLibs = "scalalibrary-2.9.1.final"
      Seq(new File(scalaLibs))
    }
          
    val allJars = {
      val libJars = libDirs.map(libDir => libDir.listFiles(
      		new java.io.FilenameFilter {
        override def accept(dir: File, name: String) = {name.endsWith(".jar")}
      })).flatten
      libJars
    }

    new ScalaPresentationCompiler(sourceFiles, allJars)
  }
    // Map of original java.io.File => its representation as a PresentationCompiler.SourceFile
  lazy val sourceFileMap = new scala.collection.mutable.HashMap[File, SourceFile]
        
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
  
    //
  // Get a list of all files in the given directory, with file name filtered by
  // regex. The recurse function indicates whether to recurse into a directory
  // or not.
  //
  def scanFiles(dir: File, regex: scala.util.matching.Regex, recurse: (File) => Boolean = {_=>true}): Seq[File] = {
    if(dir.isDirectory && recurse(dir)) {
        dir.listFiles.toSeq.collect({
            case f if f.isFile && regex.unapplySeq(f.getName).isDefined => Seq(f)
            case f if f.isDirectory => scanFiles(f, regex)
        }).flatten
    } else {
        Nil
    }
  }
  
  def scanCompilableFiles(dir: File) = scanFiles(dir, "^[^.].*[.](scala|java)$".r)
  
  //update the compiler with new or removed files
  def update() {
    compiler.loadSources(sourceFiles)
  }
  
  //this is for compiling
  def compile(filePath: String): Seq[PresentationCompiler.Problem] = {
    update()
    sourceFileMap.get(new File(filePath)).map(compiler.compile).getOrElse(Seq())
  }
  
  //this is for auto-completing
  def complete(filePath: String, line: Int, column: Int): Seq[PresentationCompiler.CompleteOption] = {
    update()
    sourceFileMap.get(new File(filePath)).map(compiler.complete(_, line, column)).getOrElse(Seq())
  }
  
}
