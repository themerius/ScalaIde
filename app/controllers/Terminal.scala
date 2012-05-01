package controllers

import scala.sys.process._

import play.api.libs.json._


object Terminal {

  def sendCommand(command: String) = {
    JsObject( Seq(
      "command" -> JsString("terminal:response"),
      "text" -> JsString( command.!! )
      )
    ).as[JsValue]
  }

}
