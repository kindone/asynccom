package com.kindone.asynccom.socket

import java.util.UUID
import com.kindone.asynccom.events._
import com.kindone.timer.Timer

object PerpetualWebSocket {
  val CONNECTION_TIMEOUT_MS = 25000
  val BACKOFF_BASE_MS = 500
}

class PerpetualSocket(baseUrl: String, wsFactory: SocketFactory, timer: Timer)
    extends MessageReceiveEventDispatcher with StateContext {

  private var socket: Option[Socket] = None
  private var openTimeoutUUID: Option[UUID] = None
  private var retryTimeoutUUID: Option[UUID] = None
  private var socketState: PersistentSocketState = new Initial(this)

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
        //dom.console.info("onOpen called")
        socketState.succeed()
      })

      ws.addOnSocketCloseListener({ e: SocketOpenCloseEvent =>
        socketState.closed()
      })

      //dom.console.info("WebSocket opening: " + ws.toString + ":" + ws.ws.toString + ":" + ws.numSocketOpenEventListeners)
      socket = Some(ws)
      scheduleOpenTimeout()

    } catch {
      // TODO:
      case err: RuntimeException =>
        //case err: JavaScriptException =>
        //dom.console.info("Exception occurred in creating WebSocket object: " + err.toString())
        socketState.fail()
    }
  }

  def scheduleOpenTimeout(): Unit = {
    openTimeoutUUID = Some(timer.setTimeout(PerpetualWebSocket.CONNECTION_TIMEOUT_MS) {
      socketState.timeout()
    })
  }

  def cancelOpenTimeout(): Unit = {
    //dom.console.info("WebSocket canceled open timeout")
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

  override def changeState(newState: PersistentSocketState): Unit = {
    socketState = newState
  }

  override def cancelReconnect(): Unit = {
    retryTimeoutUUID.foreach(uuid => timer.clearTimeout(uuid))
    retryTimeoutUUID = None
  }
  /*
  def open() = {
    socket.foreach { ws =>
      //dom.console.info("WebSocket forced open event: " + ws.toString + ":" + ws.asInstanceOf[WebSocket].ws.toString + ":" + ws.numSocketOpenEventListeners)
      ws.asInstanceOf[WebSocket].ws.asInstanceOf[js.Dynamic].onopen(js.Dynamic.literal().asInstanceOf[Event])
    }
  }
*/
}
