package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{MessageReceiveEvent, SocketErrorEvent, SocketOpenCloseEvent}
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}
import org.scalatest.FlatSpec
import org.scalatest._

import scala.scalajs.js
import scala.scalajs.js.{JavaScriptException, |}
import scala.language.dynamics


trait MockWebSocketTrait extends js.Object {
  def send(str: String): Unit
  def close(code: Int, reason: String): Unit
  var onopen: js.Function1[Event, _]
  var onerror: js.Function1[ErrorEvent, _]
  var onmessage: js.Function1[MessageEvent, _]
  var onclose: js.Function1[CloseEvent, _]
  def toString():String
}

class DisabledWebSocketTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {

  // scalamock has incompatibility with scalajs js.Object valueOf
  ignore should "remove event listeners on close" in {
    val bws = mock[MockWebSocketTrait]

    inSequence {
      (bws.send _).expects(*).throws(new JavaScriptException("synthetic error message 1"))
      (bws.close _).expects(100, "normal")
    }

    val ws = new WebSocket(Some(bws))

    var openCalled = 0
    var closeCalled = 0
    var errorCalled = 0
    var messageCalled = 0

    println("initialize WebSocket with mock base socket")

    ws.addOnSocketOpenListener((_:SocketOpenCloseEvent) => {
      openCalled += 1
    })

    ws.addOnSocketCloseListener((_:SocketOpenCloseEvent) => {
      closeCalled += 1
    })

    ws.addOnErrorListener((_:SocketErrorEvent) => {
      errorCalled += 1
    })

    ws.addOnReceiveListener((_:MessageReceiveEvent) => {
      messageCalled += 1
    })


    Then("added event listeners")


    ws.dispatchSocketOpenEvent()
    ws.dispatchErrorEvent("error")
    ws.dispatchSocketCloseEvent()
    ws.dispatchReceiveEvent("message")

    Then("dispatch socket open event")

    openCalled should be(1)
    errorCalled should be(1)
    closeCalled should be(1)
    messageCalled should be(1)

    ws.close()

    Then("close socket")

    // all event listeners should be removed

    ws.dispatchSocketOpenEvent()
    ws.dispatchErrorEvent("error")
    ws.dispatchSocketCloseEvent()
    ws.dispatchReceiveEvent("message")

    openCalled should be(1)
    errorCalled should be(1)
    closeCalled should be(1)
    messageCalled should be(1)
  }

  ignore should "work as expected" in {
//    val real = mock[org.scalajs.dom.raw.WebSocket]

    val real = new org.scalajs.dom.raw.WebSocket()
    val fake = new MockWebSocketTrait {override def close(code: Int, reason: String): Unit = ???

      override def send(str: String): Unit = ???

      override var onerror: js.Function1[ErrorEvent, _] = _
      override var onopen: js.Function1[Event, _] = _
      override var onmessage: js.Function1[MessageEvent, _] = _
      override var onclose: js.Function1[CloseEvent, _] = _
    }
    def either(ws:org.scalajs.dom.raw.WebSocket | WebSocketLike) = {
//      ws.merge.onopen(js.Dynamic.literal().asInstanceOf[Event])
    }

    def structural(ws: {
      var onopen:js.Function1[Event, _]
    }) = {
      ws.onopen(js.Dynamic.literal().asInstanceOf[Event])
    }

    either(real)
    either(fake)

    structural(real)
    structural(fake)
  }


}
