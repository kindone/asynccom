package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{MessageReceiveEvent, SocketErrorEvent, SocketOpenCloseEvent}
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}
import org.scalatest.FlatSpec
import org.scalatest._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

import scala.language.dynamics

class GoodWebSocket extends WebSocketTrait {
  def send(str: String): Unit = {}
  def close(code: Int, reason: String): Unit = {}
  override def toString():String = ""

  var onopen:js.Function1[Event, _] = null
  var onerror: js.Function1[ErrorEvent, _] = null
  var onmessage: js.Function1[MessageEvent, _] = null
  var onclose: js.Function1[CloseEvent, _] = null
}


class WebSocketTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {
  "Socket" should "add and emit event properly" in {
    val socket = new Socket {
      override def close(): Unit = ???

      override def send(str: String): Unit = ???

      override def open(url: String): Unit = ???
    }

    var openCalled = false
    var closeCalled = false
    var errorCalled = false
    var messageCalled = false
    var errorMessage = ""
    var message = ""

    socket.addOnSocketOpenListener((e:SocketOpenCloseEvent) => {
      openCalled = true
    })

    socket.addOnSocketCloseListener((e:SocketOpenCloseEvent) => {
      closeCalled = true
    })

    socket.addOnErrorListener((e:SocketErrorEvent) => {
      errorMessage = e.message
      errorCalled = true
    })

    socket.addOnReceiveListener((e:MessageReceiveEvent) => {
      message = e.str
      messageCalled = true
    })

    socket.dispatchSocketOpenEvent()
    openCalled should be(true)
    socket.dispatchSocketCloseEvent()
    closeCalled should be(true)
    socket.dispatchErrorEvent("error")
    errorCalled should be(true)
    socket.dispatchReceiveEvent("message")
    messageCalled should be(true)

    socket.numSocketOpenEventListeners should be(1)
    socket.numSocketCloseEventListeners should be(1)

    errorMessage should be("error")
    message should be("message")
  }

  "WebSocket" should "receive object with send and close methods" in {
    val good = new WebSocket(Some(new GoodWebSocket))
    good.send("")
    good.close()
  }



  "WebSocket" should "emit events on on* call" in {
    val base = new GoodWebSocket
    val ws = new WebSocket(Some(base))

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

    base.onopen(js.Dynamic.literal().asInstanceOf[Event])
    openCalled should be(true)
    base.onclose(js.Dynamic.literal().asInstanceOf[CloseEvent])
    closeCalled should be(true)
    base.onerror(js.Dynamic.literal(message = "error").asInstanceOf[ErrorEvent])
    errorCalled should be(true)
    base.onmessage(js.Dynamic.literal(data = "message").asInstanceOf[MessageEvent])
    messageCalled should be(true)

    errorMessage should be("error")
    message should be("message")
  }


}
