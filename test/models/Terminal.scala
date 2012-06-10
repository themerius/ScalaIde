import org.specs2.mutable._
import org.specs2.mock._

import play.api.libs.json._


object CommunicationMock extends Mockito {
  val m = mock[models.ICommunication].smart
  val inner = mock[play.api.libs.iteratee.PushEnumerator[JsValue]].smart
  //inner.push(js) returns true
  m.out returns inner
}

object TerminalTestConfig extends models.TerminalContext {
  lazy val terminal = new Terminal
  override protected val communication = CommunicationMock.m
}


class TerminalSpec extends Specification {

  "Before the terminal is started" should {
    "no stdin be set" in {
      TerminalTestConfig.terminal.input must beNull
    }
    "it be deactivated" in {
      TerminalTestConfig.terminal.deactivated must_== true
    }
  }

  "The 'start' method" should {
    "spawn a new terminal-bash" in {
      TerminalTestConfig.terminal.start must beAnInstanceOf[Any]
    }
    "and it's deactivated on Microsoft Windows, and activated on UNIX" in {
      if (System.getProperty("os.name").startsWith("Windows"))
        TerminalTestConfig.terminal.deactivated must_== true
      else
        TerminalTestConfig.terminal.deactivated must_== false
    }
  }


}

