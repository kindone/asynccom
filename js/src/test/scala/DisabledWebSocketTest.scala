package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{MessageReceiveEvent, SocketErrorEvent, SocketOpenCloseEvent}
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}
import org.scalatest.FlatSpec
import org.scalatest._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException

import scala.language.dynamics


trait MockWebSocketTrait extends js.Object {
  def send(str: String): Unit
  def close(code: Int, reason: String): Unit
}


class DisabledWebSocketTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {

  // scalamock has incompatibility with scalajs js.Object valueOf
  ignore should "remove event listeners on close" in {
    val bws = mock[MockWebSocketTrait]

    inSequence {
      (bws.send _).expects(*).throws(new JavaScriptException("synthetic error message 1"))
      (bws.close _).expects(100, "normal")
    }

    val ws = new WebSocket(bws)

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




}
