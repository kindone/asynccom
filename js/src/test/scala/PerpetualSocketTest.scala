package com.kindone.asynccom.socket

import com.kindone.timer._
import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}
import org.scalajs.dom
import org.scalatest.FlatSpec
import org.scalatest._

import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.annotation.{JSGlobal, ScalaJSDefined}

/**
 * Created by kindone on 2017. 2. 18..
 */

class MockWebSocket extends js.Object {

  def send(str: String): Unit = {
    sendCalled = true
  }
  def close(code: Int, reason: String): Unit = {}
//  var onopen: js.Function1[Event, _] = null
//  var onerror: js.Function1[ErrorEvent, _] = null
//  var onmessage: js.Function1[MessageEvent, _] = null
//  var onclose: js.Function1[CloseEvent, _] = null
  var sendCalled: Boolean = false
}

trait MockSocketFactory extends SocketFactory {
  def create(url: String): Socket
}

class MockSocket(val socket:MockWebSocket) extends Socket
{
  override def close(): Unit = {
    socket.close(0,  "")
  }

  override def send(str: String): Unit = {
    socket.send(str)
  }

}

class MockPerpetualSocket(url:String, socketFactory:SocketFactory, timer:Timer)
  extends PerpetualSocket(url, socketFactory, timer)
{
  def simulateOpen() = {
    getSocket().foreach { socket =>
      println("WebSocket forced open event: " + socket.toString + ":" +  socket.asInstanceOf[MockSocket].socket.toString +":" + socket.numSocketOpenEventListeners)
      socket.dispatchSocketOpenEvent()
    }
  }
}


class PerpetualSocketTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {

  "PerpetualSocket" should "connect can fail" in {
    val factory = mock[MockSocketFactory]
    (factory.create _).expects(*).throws(new JavaScriptException("synthetic error message"))

    val timer = new TestableTimer
    val pws = new PerpetualSocket("", factory, timer)
  }

  "PerpetualSocket" should "wait for reconnect on failed connect attempt" in {
    val factory = mock[MockSocketFactory]

    (factory.create _).expects(*).throws(new JavaScriptException("synthetic error message"))

    val timer = new TestableTimer
    val pws = new PerpetualSocket("", factory, timer)

    timer.advance(PerpetualSocket.BACKOFF_BASE_MS - 1)

    //    (factory.create _).expects(*).throws(new JavaScriptException("synthetic error message"))
    timer.firedEntries.size should be(0)

    println("")
  }

  "PerpetualSocket" should "reconnect on failed connect attempt after wait" in {
    val factory = mock[MockSocketFactory]

    //reconnection will be tried and succeed after third attempt
    inSequence {
      // fail first attempt
      (factory.create _).expects(*).throws(new JavaScriptException("synthetic error message 1"))
      // fail again to see the timer rescheduling
      (factory.create _).expects(*).throws(new JavaScriptException("synthetic error message 2"))
      // succeed
      (factory.create _).expects(*).returns(new MockSocket(new MockWebSocket))
      (factory.create _).expects(*).returns(new MockSocket(new MockWebSocket))
    }

    // there should be one timer for retry (fail 1)
    val timer = new TestableTimer

    Given("a new instance")
    val pSocket = new MockPerpetualSocket("", factory, timer)
    Then("timer should be scheduled as it failed to create websocket")
    timer.firedEntries.size should be(0)
    timer.scheduledEntries.size should be(1)

    println("time advanced by 500ms ")
    // timer will fire at first backoff and reschedule another (fail 2)
    timer.advance(PerpetualSocket.BACKOFF_BASE_MS)

    println("timer should fire and reschedule another since it fails again")
    timer.firedEntries.size should be(1)
    timer.scheduledEntries.size should be(1)

    println("time advanced by another 1000ms")
    timer.advance(PerpetualSocket.BACKOFF_BASE_MS * 2)

    println("timer was fired and creating websocket succeeds, but timer remains to be fired")
    timer.firedEntries.size should be(2)
    timer.scheduledEntries.size should be(1)

    println("time advanced by another 2000ms")
    //
    timer.advance(PerpetualSocket.BACKOFF_BASE_MS * 4)
    timer.firedEntries.size should be(2)
    timer.scheduledEntries.size should be(1)
    timer.canceledEntries.size should be(0)

    timer.advance(PerpetualSocket.CONNECTION_TIMEOUT_MS)
    timer.firedEntries.size should be(3)
    timer.advance(PerpetualSocket.BACKOFF_BASE_MS * 8)
    println("socket emits open event")

    pSocket.simulateOpen()

    println("onopen timer is canceled")
    timer.firedEntries.size should be(4)
    timer.scheduledEntries.size should be(0)
    timer.canceledEntries.size should be(1)

    When("nothing to fire and time flows")
    timer.advance(PerpetualSocket.CONNECTION_TIMEOUT_MS * 100)

    Then("nothing happens")
    timer.firedEntries.size should be(4)
    timer.scheduledEntries.size should be(0)
    timer.canceledEntries.size should be(1)
  }

}
