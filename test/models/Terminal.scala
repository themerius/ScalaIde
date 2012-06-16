import org.specs2.mutable._
import org.specs2.mock._

import play.api.libs.json._
import scala.sys.process._

import models.{Terminal, ExpectScript}


object WebsocketMock extends Mockito {
  val m = mock[play.api.libs.iteratee.PushEnumerator[JsValue]].smart
}

class ExpectScriptSpec extends Specification {

  val ec = new ExpectScript

  "The 'generateStr' method" should {
    "generate a valid expect script" in {
      val result = ec.generateStr("user", "example.tdl", "users_password")
      result must_== """#!/usr/bin/expect -f

spawn ssh user@example.tdl
expect "user@example.tdl's password:"
send "users_password\n"
interact"""
    }
  }

  "The 'createFile' method" should {
    "create a file in /tmp" in {
      val filename = ec.createFile
      filename must startWith("/tmp/ScalaIde")
      ("ls /tmp | grep "+filename).!! must_== filename
    }
    "and save the filename in itself" in {
      ec.filename must startWith("/tmp/ScalaIde")
    }
  }

  "The 'delFile' method" should {
    "delete the file generated from 'createFile'" in {
      val filename = ec.filename
      ec.delFile
      ("ls /tmp | grep "+filename).!! must_== ""
    }
  }

}

class TerminalSpec extends Specification {

  val terminal = new Terminal
  terminal.setWebsocket(WebsocketMock.m)

  "Before the terminal is started" should {
    "no stdin be set" in {
      terminal.input must beNull
    }
    "it be deactivated" in {
      terminal.deactivated must_== true
    }
  }

  "The 'start' method" should {
    "spawn a new (remote) terminal" in {
      terminal.start must beAnInstanceOf[Any]
    }
    "and it's deactivated on Microsoft Windows, and activated on UNIX" in {
      if (System.getProperty("os.name").startsWith("Windows"))
        terminal.deactivated must_== true
      else
        terminal.deactivated must_== false
    }
  }


}

