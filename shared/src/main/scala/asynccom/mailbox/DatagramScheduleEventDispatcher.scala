package com.kindone.asynccom.mailbox

import com.kindone.event.{Event, EventDispatcher, EventListener}

class DatagramScheduleEvent extends Event
class RetransmitTimerExpiredEvent extends DatagramScheduleEvent
class HeartbeatTimerExpiredEvent extends DatagramScheduleEvent
class UnresponsiveEvent extends DatagramScheduleEvent
class FatalErrorEvent extends DatagramScheduleEvent

trait DatagramSchedulerEventDispatcher {
  private val dispatcher: EventDispatcher[DatagramScheduleEvent] = new EventDispatcher

  val RETRANSMIT = "retransmit"
  val HEARTBEAT = "heartbeat"
  val UNRESPONSIVE = "unresponsive"
  val FATAL = "fatal"

  def addOnRetransmitEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.addEventListener(RETRANSMIT, handler)

  def removeOnRetransmitEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.removeEventListener(RETRANSMIT, handler)

  def dispatchRetransmitEvent() =
    dispatcher.dispatchEvent(RETRANSMIT, new RetransmitTimerExpiredEvent)

  def removeAllRetransmitEventListeners(): Unit = {
    dispatcher.clear(RETRANSMIT)
  }


  def addOnHeartbeatEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.addEventListener(HEARTBEAT, handler)

  def removeOnHeartbeatEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.removeEventListener(HEARTBEAT, handler)

  def dispatchHeartbeatEvent() =
    dispatcher.dispatchEvent(HEARTBEAT, new HeartbeatTimerExpiredEvent)

  def removeAllHeartbeatEventListeners(): Unit = {
    dispatcher.clear(HEARTBEAT)
  }
  

  def addOnUnresponsiveEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.addEventListener(UNRESPONSIVE, handler)

  def removeOnUnresponsiveEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.removeEventListener(UNRESPONSIVE, handler)

  def dispatchUnresponsiveEvent() =
    dispatcher.dispatchEvent(UNRESPONSIVE, new UnresponsiveEvent)

  def removeAllUnresponsiveEventListeners(): Unit = {
    dispatcher.clear(UNRESPONSIVE)
  }

  def addOnFatalErrorEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.addEventListener(FATAL, handler)

  def removeOnFatalErrorEventListener(handler: EventListener[DatagramScheduleEvent]) =
    dispatcher.removeEventListener(FATAL, handler)

  def dispatchFatalErrorEvent() =
    dispatcher.dispatchEvent(FATAL, new FatalErrorEvent)

  def removeAllFatalErrorEventListeners(): Unit = {
    dispatcher.clear(FATAL)
  }

}
