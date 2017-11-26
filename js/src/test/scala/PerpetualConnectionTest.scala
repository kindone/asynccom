package com.kindone.asynccom.socket

import asynccom.connection.{PerpetualConnection, PerpetualConnectionConfig}
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


class SimulatedPerpetualConnectionConfig(url:String, factory:SocketFactory)
  extends PerpetualConnectionConfig(url, factory, new SimulatedTimeline) {

}

class MockWebSocket extends js.Object {

  def open(url: String):Unit = {
  }

  def send(str: String): Unit = {
    sendCalled = true
  }
  def close(code: Int, reason: String): Unit = {}
  var onopen: js.Function1[Event, _] = null
  var onerror: js.Function1[ErrorEvent, _] = null
  var onmessage: js.Function1[MessageEvent, _] = null
  var onclose: js.Function1[CloseEvent, _] = null
  var sendCalled: Boolean = false
}

trait MockSocketFactory extends SocketFactory {
  def create(): Socket
}

class MockSocket(val socket:MockWebSocket) extends Socket
{
  override def open(url: String): Unit = {
    socket.open(url)
  }

  override def close(): Unit = {
    socket.close(0,  "")
  }

  override def send(str: String): Unit = {
    socket.send(str)
  }
}

class MockPerpetualConnection(url:String, socketFactory:SocketFactory, timeline:Timeline)
  extends PerpetualConnection(new PerpetualConnectionConfig(url, socketFactory, timeline))
{
  def simulateOpen() = {
    getSocket().foreach { socket =>
//      println("WebSocket forced open event: " + socket.toString  + ":" + socket.numSocketOpenEventListeners)
      socket.dispatchSocketOpenEvent()
    }
  }

  def simulateClose() = {
    getSocket().foreach { socket =>
//      println("WebSocket forced close event: " + socket.toString  + ":" + socket.numSocketCLoseEventListeners)
      socket.dispatchSocketCloseEvent()
    }
  }
}


class PerpetualConnectionTest extends FlatSpec with org.scalamock.scalatest.MockFactory with Matchers with GivenWhenThen {

  "PerpetualSocket" should "be able to fail at connect() by failed socket open()" in {
    val factory = new SocketFactory {
      override def create(): Socket = {
        val socket = mock[Socket]
        (socket.addOnReceiveListener _).expects(*)
        (socket.addOnSocketOpenListener _).expects(*)
        (socket.addOnSocketCloseListener _).expects(*)
        (socket.open _).expects(*).throws(new JavaScriptException("synthetic error message"))
        socket
      }
    }

    val conn = new PerpetualConnection(new SimulatedPerpetualConnectionConfig("", factory))
    conn.initiate()
  }

  "PerpetualSocket" should "wait for reconnect on failed connect()" in {
    val factory = new SocketFactory {
      override def create(): Socket = {
        val socket = mock[Socket]
        (socket.addOnReceiveListener _).expects(*)
        (socket.addOnSocketOpenListener _).expects(*)
        (socket.addOnSocketCloseListener _).expects(*)
        (socket.open _).expects(*).throws(new JavaScriptException("synthetic error message"))
        socket
      }
    }

    val timeline = new SimulatedTimeline
    val config = new PerpetualConnectionConfig("", factory, timeline)
    val conn = new PerpetualConnection(config)
    conn.initiate()
    timeline.scheduledEntries.size should be(1)
    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS - 1)
    timeline.firedEntries.size should be(0)
  }

  "PerpetualSocket" should "reconnect on failed connect attempt after wait" in {

    def failingSocket = {
      val socket = mock[Socket]
      (socket.addOnReceiveListener _).expects(*)
      (socket.addOnSocketOpenListener _).expects(*)
      (socket.addOnSocketCloseListener _).expects(*)
      (socket.open _).expects(*).throwing(new JavaScriptException())
      socket
    }
    val failingSocket1 = failingSocket
    val failingSocket2 = failingSocket

    def stubSocket = new Socket {
      override def close(): Unit = {}

      override def send(str: String): Unit = {}

      override def open(url: String): Unit = {}
    }

    val factory = mock[SocketFactory]
    inSequence {
      (factory.create _).expects().returning(failingSocket1)
      (factory.create _).expects().returning(failingSocket2)
      (factory.create _).expects().returning(stubSocket)
      (factory.create _).expects().returning(stubSocket)

      (factory.create _).expects().returning(stubSocket)
    }


    // there should be one timer for retry (fail 1)
    val timeline = new SimulatedTimeline

    Given("a new Connection instance ")
    val conn = new MockPerpetualConnection("", factory, timeline)

    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)
    timeline.firedEntries.size should be(0)
    timeline.scheduledEntries.size should be(0)
    conn.stateName should be ("Initial")

    When("Connection is initiated")
    conn.initiate() // socket open fails by mock sequence
    Then("retry timer should be scheduled as it fails to create websocket")
    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)
    conn.stateName should be ("ConnectionRefused(0)")
    timeline.firedEntries.size should be(0)
    timeline.scheduledEntries.size should be(1)

    When(s"time advanced by ${PerpetualConnection.BACKOFF_BASE_MS}ms")

    // timer will fire at first backoff and reschedule another (fail 2)
    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS)

    Then("timer should fire and reschedule another since it fails again")
    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)

    timeline.firedEntries.size should be(1)
    timeline.scheduledEntries.size should be(1)
    conn.isOpen should be(false)
    conn.stateName should be ("ConnectionRefused(1)")

    When(s"time advanced by another ${PerpetualConnection.BACKOFF_BASE_MS * 2}ms" )
    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS * 2)

    Then("timer was fired and creating websocket succeeds, but open timeout timer is scheduled")
    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)
    timeline.firedEntries.size should be(2)
    timeline.scheduledEntries.size should be(1)
    conn.stateName should be ("WaitingForOpen")

//    When(s"time advanced by another ${PerpetualConnection.BACKOFF_BASE_MS * 4}ms")
//    //
//    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS * 4)
//    Then("timer this time does")
//    timeline.firedEntries.size should be(3)
//    timeline.scheduledEntries.size should be(1)
//    timeline.canceledEntries.size should be(0)

    When(s"time advanced by another ${PerpetualConnection.OPEN_TIMEOUT_MS}ms")
    timeline.advance(PerpetualConnection.OPEN_TIMEOUT_MS)

    Then("open timeout timer should fire")
    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)
    timeline.firedEntries.size should be(3)
    timeline.scheduledEntries.size should be(1)
    timeline.canceledEntries.size should be(0)
    conn.stateName should be ("WaitingForOpen")

    When(s"time advanced by another ${PerpetualConnection.BACKOFF_BASE_MS}ms")
    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS)
    Then("nothing happens")
    timeline.firedEntries.size should be(3)
    timeline.scheduledEntries.size should be(1)
    timeline.canceledEntries.size should be(0)
    conn.stateName should be ("WaitingForOpen")

    conn.getSocket().foreach { s =>
      info(s"number of event listeners: ${s.numSocketOpenEventListeners}")
    }
    When("socket emits open event")
    conn.simulateOpen()

    Then("connected and onopen timer is canceled")
    info("  fired: " + timeline.firedEntries.toString)
    info("  scheduled: " + timeline.scheduledEntries.toString)
    info("  canceled: " + timeline.canceledEntries.toString)

    timeline.firedEntries.size should be(3)
    timeline.scheduledEntries.size should be(0)
    timeline.canceledEntries.size should be(1)
    conn.stateName should be ("Connected")

    When("time flows")
    timeline.advance(PerpetualConnection.OPEN_TIMEOUT_MS * 100)

    Then("nothing happens and connection is still connected")
    timeline.firedEntries.size should be(3)
    timeline.scheduledEntries.size should be(0)
    timeline.canceledEntries.size should be(1)
    conn.stateName should be ("Connected")

    // TODO: test close and reconnect
    When("connection is closed")
    conn.simulateClose()
    timeline.firedEntries.size should be(3)
    timeline.scheduledEntries.size should be(1)
    timeline.canceledEntries.size should be(1)
    conn.stateName should be ("ConnectionClosed")

    When(s"time advanced by ${PerpetualConnection.BACKOFF_BASE_MS}ms")
    timeline.advance(PerpetualConnection.BACKOFF_BASE_MS)

    Then("connection is retried")
    timeline.firedEntries.size should be(4)
    timeline.scheduledEntries.size should be(1)
    timeline.canceledEntries.size should be(1)
    conn.stateName should be ("WaitingForOpen")

  }

}
