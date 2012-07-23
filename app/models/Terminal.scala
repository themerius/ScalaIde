package models

import scala.sys.process._
import java.io.{OutputStreamWriter, FileOutputStream}

import play.api.libs.iteratee._
import play.api.libs.json._

class ExpectScript {

  var filename: String = _
  var str: String = _

  def generateStr(user: String, url: String, passw: String): String = {
    var str = """#!/usr/bin/expect -f

spawn ssh %s@%s
expect "%s@%s's password:"
send "%s\n"
interact""".format(user, url, user, url, passw)
    this.str = str
    return str
  }

  def createFile: String = {
    val filename = java.util.UUID.randomUUID()
    this.filename = "/tmp/ScalaIde" + filename.toString

    val file = new OutputStreamWriter(
      new FileOutputStream(this.filename), "UTF-8")
    file.append(this.str)
    file.close

    return this.filename
  }

  def delFile = ("rm " + this.filename).!
}

// Cake Pattern for dependency injection
class Terminal {

  var input: java.io.OutputStream = _
  var websocket: PushEnumerator[JsValue] = _
  var deactivated = true

  def start(id: String) = {
  
    User.findById(id).map { user =>
      if (System.getProperty("os.name").startsWith("Windows")) {
        deactivated = true

        println("This feature only available on unix.")
        sendToWebsocket("This feature only available on unix.")
      } else if (user.public) {
        deactivated = true

        println("This feature is only available for certain user.")
        sendToWebsocket("This feature is only available for certain user.")
      
      } else {
        deactivated = false

        val expectScript = new ExpectScript
        expectScript.generateStr("terminal", "141.37.31.235", "")
        val scriptPath = expectScript.createFile

        val pio = new ProcessIO(this.stdin, this.stdout, this.stderr)
        ("expect -f " + scriptPath).run(pio)

        expectScript.delFile
      }
    }
  }

  def close = {
    //this.handleKey("4".toByte)
    if (!deactivated)
      this.input.close()
  }

  def stdin(in: java.io.OutputStream) = {
    this.input = in
  }

  def stdout(out: java.io.InputStream) = {
    val lines = scala.io.Source.fromInputStream(out).getLines
    def inner(str: String) = sendToWebsocket(str)
    lines.foreach(inner)
  }

  def stderr(out: java.io.InputStream) = {
    val lines = scala.io.Source.fromInputStream(out).getLines
    def inner(str: String) = sendToWebsocket(str)
    lines.foreach(inner)
  }

  def setWebsocket(ws: PushEnumerator[JsValue]) {
    this.websocket = ws
  }

  def sendToWebsocket(output: String) = this.synchronized {
    val msg = JsObject( Seq(
        "type" -> JsString("terminal"),
        "command" -> JsString("response"),
        "value" -> JsString( output )
        )
      ).as[JsValue]
    websocket.push(msg)
  }

  def handleKey(receivedKey: Byte) = {
    if (deactivated) {
      println("Terminal-feature deactivated.")
    } else {
      input.write(receivedKey)
      input.flush()
    }
  }
}
