package com.kindone.asynccom

import asynccom.client.Client
import asynccom.connection.{PerpetualConnection, PerpetualConnectionConfig}
import com.kindone.asynccom.events.MessageReceiveEvent
import com.kindone.asynccom.socket.{Socket, SocketFactory}
import com.kindone.event._
import com.kindone.timer.SimulatedTimeline
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class GenericModule(baseUrl:String) {
  import com.softwaremill.macwire._

  lazy val timeline = wire[SimulatedTimeline]
  lazy val socketFactory = new SocketFactory {
    override def create(): Socket = new Socket {
      override def open(url: String): Unit = {}

      override def close(): Unit = {}

      override def send(str: String): Unit = {}
    }
  }
  lazy val connectionConfig = wire[PerpetualConnectionConfig]
}


class ServerClientTest extends FlatSpec with MockFactory with Matchers {

  "client" should "be able to connect to server" in {


    val module = new GenericModule("")

    val client = new Client(module.connectionConfig)
    client.initiate()
    val message = "c" :: "b" :: "a" :: Nil
    client.setMailbox(message)
    client.addOnReceiveListener((evt:MessageReceiveEvent) => {
      evt.str
    })
  }
}