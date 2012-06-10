package models

import scala.sys.process._
import play.api.libs.json._

// Cake Pattern for dependency injection
trait TerminalContext {
  class Terminal {

    var input: java.io.OutputStream = _
    var deactivated = true

    def start = {
      if (System.getProperty("os.name").startsWith("Windows")) {
        deactivated = true
        println("This feature only available on unix.")
        sendToWebsocket("This feature only available on unix.")
      } else {
        deactivated = false
        val pio = new ProcessIO(this.stdin, this.stdout, this.stderr)
        "bash -il".run(pio)
      }
    }

    def stdin(in: java.io.OutputStream) = {
      this.input = in
    }

    def stdout(out: java.io.InputStream) = {
      val lines = scala.io.Source.fromInputStream(out).getLines
      def inner(str: String) = sendToWebsocket("    " + str)
      lines.foreach(inner)
    }

    def stderr(out: java.io.InputStream) = {
      val lines = scala.io.Source.fromInputStream(out).getLines
      def inner(str: String) = sendToWebsocket(str)
      lines.foreach(inner)
    }

    def sendToWebsocket(output: String) = this.synchronized {
      val msg = JsObject( Seq(
          "type" -> JsString("terminal"),
          "command" -> JsString("response"),
          "value" -> JsString( output )
          )
        ).as[JsValue]
      communication.out.push(msg)
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

  protected val communication: ICommunication
}


// Configuration
object Terminal extends TerminalContext {
  lazy val terminal = new Terminal
  override protected val communication = Communication
}
