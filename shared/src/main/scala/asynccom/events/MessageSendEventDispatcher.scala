package com.kindone.asynccom.events

import com.kindone.event.{ EventListener, EventDispatcher }

/**
 * Created by kindone on 2016. 5. 31..
 */

case class MessageSendEvent(str: String) extends SocketEvent

trait MessageSendEventDispatcher {
  private val dispatcher: EventDispatcher[MessageSendEvent] = new EventDispatcher

  val SEND = "send"
  val HEARTBEAT = "heartbeat"

  def addOnSendListener(handler: EventListener[MessageSendEvent]) =
    dispatcher.addEventListener(SEND, handler)

  def removeOnSendListener(handler: EventListener[MessageSendEvent]) =
    dispatcher.removeEventListener(SEND, handler)

  def dispatchSendEvent(str: String) =
    dispatcher.dispatchEvent(SEND, new MessageSendEvent(str))

  def removeAllOnSendListener(): Unit = {
    dispatcher.clear(SEND)
  }

  def addOnHeartbeatListener(handler: EventListener[MessageSendEvent]) =
    dispatcher.addEventListener(HEARTBEAT, handler)

  def removeOnHeartbeatListener(handler: EventListener[MessageSendEvent]) =
    dispatcher.removeEventListener(HEARTBEAT, handler)

  def dispatchHeartbeatEvent() =
    dispatcher.dispatchEvent(HEARTBEAT, new MessageSendEvent(""))

  def removeAllOnHeartbeatListener(): Unit = {
    dispatcher.clear(HEARTBEAT)
  }

}

