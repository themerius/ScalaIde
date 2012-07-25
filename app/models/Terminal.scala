package models

import scala.sys.process._
import java.io.{OutputStreamWriter, FileOutputStream}

import play.api.libs.iteratee._
import play.api.libs.json._
import play.api.Play

/** UNIX only: builds a `except` temporary script
  * with the ability to connect via ssh. */
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

/** UNIX only: establishes a ssh connection and lets the user communicate with.
  * Listens on stadard-out and standard-in and sends every character via
  * websocket to the users frontend.
  * If the user types something, the characters are received via websocket
  * and sended to standard-in.
  * Automatically deactivates if MS Windows is used (on server-side). */
class Terminal {

  var input: java.io.OutputStream = _
  var websocket: PushEnumerator[JsValue] = _
  var sshUser = ""
  var sshIp = ""
  var sshPwd = ""
  var deactivated = true
  var publicUser = false  // public users have a disabled terminal

  def start = {
    if (!Play.current.configuration.getBoolean("terminal.support").get) {
      deactivated = true

      println("This feature only available on unix.")
      sendToWebsocket("This feature only available on unix.")
    } else if (publicUser) {
      println("This feature is only available for certain user.")
      sendToWebsocket("This feature is only available for certain user.")
    } else {
      deactivated = false

      val expectScript = new ExpectScript
      expectScript.generateStr(this.sshUser, sshIp, sshPwd)
      val scriptPath = expectScript.createFile

      val pio = new ProcessIO(this.stdin, this.stdout, this.stderr)
      ("expect -f " + scriptPath).run(pio)

      expectScript.delFile
    }
  }

  def close = {
    //this.handleKey("4".toByte)
    if (!deactivated)
      this.input.close()
  }

  def deactivateIfPublic(userId: String) = {
    User.findById(userId).map { user =>
      if (user.sshlogin == "") {
        publicUser = true
      } else {
        publicUser = false
      }
    }
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

  def getSshLoginData(userId: String) = {
    User.findById(userId).map { user =>
      val sshlogin = user.sshlogin.split("@")
      if (sshlogin.length == 2) {
        this.sshUser = sshlogin(0)
        this.sshIp = sshlogin(1)
        this.sshPwd = user.password
      }
    }
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
