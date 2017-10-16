package com.kindone.infinitewall.persistence.wsstorage.socket

import java.util.UUID

import com.kindone.infinitewall.event.EventListener
import com.kindone.infinitewall.persistence.wsstorage.events._
import com.kindone.infinitewall.util.Timer
import org.scalajs.dom.raw.Event
import upickle.default._
import org.scalajs.dom
import scala.scalajs.js
import scala.scalajs.js.JavaScriptException
import scala.scalajs.js.timers.SetTimeoutHandle

/**
 * Created by kindone on 2016. 4. 17..
 */
object PerpetualWebSocket {
  val CONNECTION_TIMEOUT_MS = 25000
  val BACKOFF_BASE_MS = 500
}

class PerpetualWebSocket(baseUrl: String, wsFactory: WebSocketFactory, timer: Timer)
    extends PerpetualSocket with StateContext {

  private var socket: Option[Socket] = None
  private var openTimeoutUUID: Option[UUID] = None
  private var retryTimeoutUUID: Option[UUID] = None
  private var socketState: PersistentWebSocketState = new Initial(this)

  socketState.tryConnect()

  def send(str: String): Unit = {
    socket.foreach(_.send(str))
  }

  def isAlive: Boolean = socket.isDefined

  def isOpen: Boolean = socketState.isInstanceOf[Connected]

  private def onReceive(e: MessageReceiveEvent) = {
    dispatchReceiveEvent(e.str)
  }

  def connect(): Unit = {
    try {
      val ws = wsFactory.create(baseUrl)
      ws.addOnReceiveListener(onReceive _)
      ws.addOnSocketOpenListener({ e: SocketOpenCloseEvent =>
        dom.console.info("onOpen called")
        socketState.succeed()
      })

      ws.addOnSocketCloseListener({ e: SocketOpenCloseEvent =>
        socketState.closed()
      })

      dom.console.info("WebSocket opening: " + ws.toString + ":" + ws.ws.toString + ":" + ws.numSocketOpenEventListeners)
      socket = Some(ws)
      scheduleOpenTimeout()

    } catch {
      case err: JavaScriptException =>
        dom.console.info("Exception occurred in creating WebSocket object: " + err.toString())
        socketState.fail()
    }
  }

  def scheduleOpenTimeout(): Unit = {
    openTimeoutUUID = Some(timer.setTimeout(PerpetualWebSocket.CONNECTION_TIMEOUT_MS) {
      socketState.timeout()
    })
  }

  def cancelOpenTimeout(): Unit = {
    dom.console.info("WebSocket canceled open timeout")
    openTimeoutUUID.foreach(uuid => timer.clearTimeout(uuid))
    openTimeoutUUID = None
  }

  override def scheduleReconnect(backOff: Int): Unit = {
    val saturatedBackOff = if (backOff < 10) backOff else 10
    val timeMs = (Math.pow(2.0, saturatedBackOff - 1) * PerpetualWebSocket.BACKOFF_BASE_MS).toLong
    retryTimeoutUUID = Some(timer.setTimeout(timeMs) {
      connect()
    })
  }

  override def changeState(newState: PersistentWebSocketState): Unit = {
    socketState = newState
  }

  override def cancelReconnect(): Unit = {
    retryTimeoutUUID.foreach(uuid => timer.clearTimeout(uuid))
    retryTimeoutUUID = None
  }

  def open() = {
    socket.foreach { ws =>
      dom.console.info("WebSocket forced open event: " + ws.toString + ":" + ws.asInstanceOf[WebSocket].ws.toString + ":" + ws.numSocketOpenEventListeners)
      ws.asInstanceOf[WebSocket].ws.asInstanceOf[js.Dynamic].onopen(js.Dynamic.literal().asInstanceOf[Event])
    }
  }
}
