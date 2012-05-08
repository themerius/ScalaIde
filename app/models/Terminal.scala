package models

import scala.sys.process._

import play.api.libs.json._


object Terminal {

  def sendCommand(command: String) = {
    JsObject( Seq(
      "type" -> JsString("terminal"),
      "command" -> JsString("response"),
      "value" -> JsString( command.!! )
      )
    ).as[JsValue]
  }

}
