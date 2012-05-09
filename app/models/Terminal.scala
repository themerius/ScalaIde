package models

import scala.sys.process._

import play.api.libs.json._


object Terminal {

  // TODO: Refactor: make concurrent (multiuser)

  var commandBuffer = ""  // new scala.collection.mutable.ArrayBuffer[Byte]

  def sendToWebsocket(output: String) = {
    val msg = JsObject( Seq(
        "type" -> JsString("terminal"),
        "command" -> JsString("response"),
        "value" -> JsString( output )
        )
      ).as[JsValue]
    Communication.out.push(msg)
  }

  def handleKey(receivedKey: Int) = {
    if (receivedKey == 13)
      invokeCommand
    else
      commandBuffer = commandBuffer + receivedKey.toChar
  }

  def invokeCommand {
    def stdin(input: java.io.OutputStream) = {
      //input.write("'x'".getBytes)
      //input.write(13)
      //input.flush()
      input.close()
    }

    def stdout(output: java.io.InputStream) = {
      val lines = scala.io.Source.fromInputStream(output).getLines
      var buffer = ""
      for (line <- lines)
        buffer = buffer + line + "\n"
      sendToWebsocket(buffer)
    }

    val pio = new ProcessIO(stdin, stdout,
      stderr => scala.io.Source.fromInputStream(stderr).getLines.foreach(println)
    )

    commandBuffer.run(pio)
    commandBuffer = ""

  }

}
