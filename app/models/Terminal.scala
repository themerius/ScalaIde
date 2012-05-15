package models

import scala.sys.process._

import play.api.libs.json._


object Terminal {

  // TODO: Refactor: make concurrent (multiuser)

  var input: java.io.OutputStream = _
  var deactivated = false

  def start = {
    if (System.getProperty("os.name").startsWith("Windows")) {
      deactivated = true
      println("This feature only available on unix, yet.")
    } else {
      val pio = new ProcessIO(this.stdin, this.stdout, this.stderr)
      "bash -il".run(pio)
    }
  }

  def stdin(in: java.io.OutputStream) = {
    this.input = in
  }

  def stdout(out: java.io.InputStream) = {
    val lines = scala.io.Source.fromInputStream(out).getLines
    def inner(str: String) = sendToWebsocket("stdout: " + str)
    lines.foreach(inner)
  }

  def stderr(out: java.io.InputStream) = {
    val lines = scala.io.Source.fromInputStream(out).getLines
    def inner(str: String) = println(str)// TODO sendToWebsocket("stderr: " + str)
    lines.foreach(inner)
  }

  def sendToWebsocket(output: String) = {
    val msg = JsObject( Seq(
        "type" -> JsString("terminal"),
        "command" -> JsString("response"),
        "value" -> JsString( output )
        )
      ).as[JsValue]
    Communication.out.push(msg)
  }

  def handleKey(receivedKey: Byte) = {
    if (deactivated) {
      println("Terminal-feature deactivated.")
    } else {
      receivedKey match {
        case 13 => {
          input.write(receivedKey)
          input.flush()
        }
        case _ => input.write(receivedKey)
      }
    }
  }

}
