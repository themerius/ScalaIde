package models

import akka.actor._
import akka.util.duration._

import play.api._
import play.api.libs.json._
import play.api.libs.iteratee._
import play.api.libs.concurrent._
import play.api.Play.current

import akka.util.Timeout
import akka.pattern.ask

object Websocket {
  implicit val timeout = Timeout(1 second)

  lazy val default = {
    val roomActor = Akka.system.actorOf(Props[Websocket])

    roomActor
  }

  def join(id:String):Promise[(Iteratee[JsValue,_],Enumerator[JsValue])] = {
    (default ? Join(id)).asPromise.map {

      case Connected(enumerator) => {

        // Create an Iteratee to consume the feed
        val iteratee = Iteratee.foreach[JsValue] { event =>
          default ! Talk(id, (event).as[JsValue])
        }.mapDone { _ =>
          default ! Quit(id)
        }

        (iteratee,enumerator)
      }

      case CannotConnect(error) => {
        // Connection error
        // A finished Iteratee sending EOF
        val iteratee = Done[JsValue,Unit]((),Input.EOF)

        // Send an error and close the socket
        val enumerator =  Enumerator[JsValue](JsObject(Seq("error" -> JsString(error)))).andThen(Enumerator.enumInput(Input.EOF))

        (iteratee,enumerator)
      }
    }
  }
}

class Websocket extends Actor {

  var members = Map.empty[String, PushEnumerator[JsValue]]
  var terminals = Map.empty[String, models.Terminal]

  def receive = {

    case Join(id) => {
      // Create an Enumerator to write to this socket
      val channel =  Enumerator.imperative[JsValue]()
      val terminal = new models.Terminal
      terminal.setWebsocket(channel)
      
      User.findById(id).map { user =>
        terminal.start(user.public)
      }

      if(members.contains(id)) {
        sender ! CannotConnect("This username is already used")
      } else {
        members = members + (id -> channel)
        terminals = terminals + (id -> terminal)
        sender ! Connected(channel)

        println(id + " connected!")

        var msg = JsObject(Seq(
          "type" -> JsString("editor"),
          "command" -> JsString("load"),
          "text" -> JsString("Happy Coding!"))
        ).as[JsValue]

        Thread.sleep(50)
        channel.push(msg)
      }
    }

    case Talk(id, text) => {
      Communication.commandHandling(text, members.getOrElse(id, null),
        terminals.getOrElse(id, null))
    }

    case Quit(id) => {
      members.getOrElse(id, null).close()
      members = members - id

      terminals.getOrElse(id, null).close
      terminals = terminals - id

      println(id + " disconnected!")
      System.gc()
    }
  }
}

case class Join(username: String)
case class Quit(username: String)
case class Talk(username: String, text: JsValue)

case class Connected(enumerator:Enumerator[JsValue])
case class CannotConnect(msg: String)
