package com.kindone.asynccom.events

import com.kindone.event.{ EventListener, EventDispatcher }

/**
 * Created by kindone on 2016. 5. 31..
 */

class SocketErrorEvent(str: String) extends WebSocketEvent

trait SocketErrorEventDispatcher {
  private val dispatcher: EventDispatcher[SocketErrorEvent] = new EventDispatcher

  val ERROR = "error"

  def addOnErrorListener(handler: EventListener[SocketErrorEvent]) =
    dispatcher.addEventListener(ERROR, handler)

  def removeOnErrorListener(handler: EventListener[SocketErrorEvent]) =
    dispatcher.removeEventListener(ERROR, handler)

  def dispatchErrorEvent(str: String) =
    dispatcher.dispatchEvent(ERROR, new SocketErrorEvent(str))

  def removeAllOnErrorListener(): Unit = {
    dispatcher.clear()
  }
}

