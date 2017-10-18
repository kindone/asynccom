package com.kindone.asynccom.events

import com.kindone.event.{ EventListener, EventDispatcher }

/**
 * Created by kindone on 2016. 5. 31..
 */

case class MessageReceiveEvent(str: String) extends WebSocketEvent

trait MessageReceiveEventDispatcher {
  private val dispatcher: EventDispatcher[MessageReceiveEvent] = new EventDispatcher

  val RECEIVE = "receive"

  def addOnReceiveListener(handler: EventListener[MessageReceiveEvent]) =
    dispatcher.addEventListener(RECEIVE, handler)

  def removeOnReceiveListener(handler: EventListener[MessageReceiveEvent]) =
    dispatcher.removeEventListener(RECEIVE, handler)

  def dispatchReceiveEvent(str: String) =
    dispatcher.dispatchEvent(RECEIVE, new MessageReceiveEvent(str))

  def removeAllOnReceiveListener(): Unit = {
    dispatcher.clear()
  }

}

