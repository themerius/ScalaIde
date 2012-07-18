import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

import java.io.{File, BufferedWriter, FileWriter}

import models.{Project, SbtProject}


class SbtProjectSpec extends Specification {

  val emptyFolder = new File("tmp_SbtProjectTestFolderEmpty")
  emptyFolder.mkdir

  val sbtFolder = new File("tmp_SbtProjectTestFolder")
  sbtFolder.mkdir
  val sbtFile = new BufferedWriter(
    new FileWriter("tmp_SbtProjectTestFolder/build.sbt")
  )
  sbtFile.write("Unit Test")
  sbtFile.close()

  val sbtEmpty = new SbtProject(emptyFolder.getAbsolutePath)
  val sbtProj = new SbtProject(sbtFolder.getAbsolutePath)

  "The 'isExistent' method" should {
    "recognize, if at the given path a sbt project is residet" in {
      sbtEmpty.isExistent must_== false
      sbtProj.isExistent must_== true
    }
  }

  "The 'doBootstrap' method" should {
    "create an empty sbt project at the given path" in {
      sbtEmpty.doBootstrap must_== true

      val lines = scala.io.Source.fromFile(
        emptyFolder.getAbsolutePath + "/build.sbt"
      ).mkString

      lines.contains("retrieveManaged := true") must_== true
    }
  }

  "The 'update' method" should {
    "perform an 'sbt update' at the given sbt project path,"
    "so that it fetches the jar files" in {
      sbtEmpty.update

      running(FakeApplication()) {
        val helper = new Project("")
        val fileList = helper.scanFiles(
          new File(emptyFolder.getAbsolutePath + "lib_managed"),
          "^[^.].*[.](jar)$".r
        )

        fileList.isEmpty must_== false
      }
    }
  }

  def deleteDir(dfile : File) : Unit = {
    if(dfile.isDirectory)
      dfile.listFiles.foreach{ f => deleteDir(f) }
    dfile.delete
  }

  deleteDir(emptyFolder)
  deleteDir(sbtFolder)

}
