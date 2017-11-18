package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{MessageReceiveEvent, SocketErrorEvent, SocketOpenCloseEvent}
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}
import org.scalatest.FlatSpec
import org.scalatest._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

import scala.language.dynamics

class GoodWebSocket extends js.Object {
  def send(str: String): Unit = {}
  def close(code: Int, reason: String): Unit = {}
}

class BadWebSocket extends js.Object {
}



class WebSocketTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {
  "WebSocket" should "receive object with send and close methods" in {
    val good = new WebSocket(new GoodWebSocket)
    good.send("")
    good.close()
    val bad = new WebSocket(new BadWebSocket)
    assertThrows[JavaScriptException] {
      bad.send("")
    }
    assertThrows[JavaScriptException] {
      bad.close()
    }
  }

  "WebSocket" should "emit events on on* call" in {
    val base = new GoodWebSocket
    val ws = new WebSocket(base)

    var openCalled = false
    var closeCalled = false
    var errorCalled = false
    var messageCalled = false
    var errorMessage = ""
    var message = ""

    ws.addOnSocketOpenListener((e:SocketOpenCloseEvent) => {
      openCalled = true
    })

    ws.addOnSocketCloseListener((e:SocketOpenCloseEvent) => {
      closeCalled = true
    })

    ws.addOnErrorListener((e:SocketErrorEvent) => {
      errorMessage = e.message
      errorCalled = true
    })

    ws.addOnReceiveListener((e:MessageReceiveEvent) => {
      message = e.str
      messageCalled = true
    })

    base.asInstanceOf[js.Dynamic].onopen(js.Dynamic.literal().asInstanceOf[Event])
    openCalled should be(true)
    base.asInstanceOf[js.Dynamic].onclose(js.Dynamic.literal().asInstanceOf[Event])
    closeCalled should be(true)
    base.asInstanceOf[js.Dynamic].onerror(js.Dynamic.literal(message = "error").asInstanceOf[ErrorEvent])
    errorCalled should be(true)
    base.asInstanceOf[js.Dynamic].onmessage(js.Dynamic.literal(data = "message").asInstanceOf[MessageEvent])
    messageCalled should be(true)

    errorMessage should be("error")
    message should be("message")
  }


}
