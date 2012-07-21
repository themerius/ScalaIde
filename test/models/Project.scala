import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

import java.io.{File, BufferedWriter, FileWriter}

import models.SbtProject


class SbtProjectSpec extends Specification {

  "The 'isExistent' method" should {
    "recognize, if at the given path a sbt project is residet" in new sbtTempProjects {
      sbtEmpty.isExistent must_== false
      sbtProj.isExistent must_== true
    }
  }

  "The 'doBootstrap' method" should {
    "create an empty sbt project at the given path" in new sbtTempProjects {
      sbtEmpty.doBootstrap must_== true

      val lines = scala.io.Source.fromFile(
        emptyFolder.getAbsolutePath + "/build.sbt"
      ).mkString

      lines.contains("retrieveManaged := true") must_== true
    }
  }

  "The 'update' method" should {
    "perform an 'sbt update' at the given sbt project path,"
    "so that it fetches the jar files" in new sbtTempProjects {
      sbtEmpty.doBootstrap
      val process = sbtEmpty.update

      process.waitFor
      val jar = new File(emptyFolder.getAbsolutePath +
        "/lib_managed/jars/org.specs2/specs2_2.9.1/specs2_2.9.1-1.11.jar"
      )

      jar.exists must_== true
    }
  }

}


trait sbtTempProjects extends After {
  val emptyFolder = new File("tmp_SbtProjectTestFolderEmpty")
  emptyFolder.mkdir

  val sbtFolder = new File("tmp_SbtProjectTestFolder")
  sbtFolder.mkdir
  val sbtFile = new BufferedWriter(
    new FileWriter("tmp_SbtProjectTestFolder/build.sbt")
  )
  sbtFile.write("Unit Test")
  sbtFile.close()

  lazy val sbtEmpty = new SbtProject(emptyFolder.getAbsolutePath)
  lazy val sbtProj = new SbtProject(sbtFolder.getAbsolutePath)

  def deleteDir(dfile: File): Unit = {
    if(dfile.isDirectory)
      dfile.listFiles.foreach{ f => deleteDir(f) }
    dfile.delete
  }

  def after = {
    deleteDir(emptyFolder)
    deleteDir(sbtFolder)
  }
}
