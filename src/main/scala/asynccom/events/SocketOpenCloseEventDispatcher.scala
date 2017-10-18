package com.kindone.asynccom.events

import com.kindone.event.{ EventListener, EventDispatcher }

/**
 * Created by kindone on 2016. 5. 31..
 */

class SocketOpenCloseEvent extends WebSocketEvent

trait SocketOpenCloseEventDispatcher {
  private val dispatcher: EventDispatcher[SocketOpenCloseEvent] = new EventDispatcher

  val SOCKET_OPEN = "socketOpen"
  val SOCKET_CLOSE = "socketClose"

  def addOnSocketOpenListener(handler: EventListener[SocketOpenCloseEvent]) =
    dispatcher.addEventListener(SOCKET_OPEN, handler)

  def removeOnSocketOpenListener(handler: EventListener[SocketOpenCloseEvent]) =
    dispatcher.removeEventListener(SOCKET_OPEN, handler)

  def dispatchSocketOpenEvent() =
    dispatcher.dispatchEvent(SOCKET_OPEN, new SocketOpenCloseEvent)

  def addOnSocketCloseListener(handler: EventListener[SocketOpenCloseEvent]) =
    dispatcher.addEventListener(SOCKET_CLOSE, handler)

  def removeOnSocketCloseListener(handler: EventListener[SocketOpenCloseEvent]) =
    dispatcher.removeEventListener(SOCKET_CLOSE, handler)

  def dispatchSocketCloseEvent() =
    dispatcher.dispatchEvent(SOCKET_CLOSE, new SocketOpenCloseEvent)

  def removeAllOnSocketOpenCloseListener(): Unit = {
    dispatcher.clear()
  }

  def numSocketOpenEventListeners = dispatcher.numEventListeners(SOCKET_OPEN)
  def numSocketCloseEventListeners = dispatcher.numEventListeners(SOCKET_CLOSE)

}

