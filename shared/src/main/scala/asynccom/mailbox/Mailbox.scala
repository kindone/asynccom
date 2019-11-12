package com.kindone.asynccom.mailbox

import com.kindone.asynccom.events.{MessageReceiveEventDispatcher, MessageSendEventDispatcher}
import com.kindone.timer.{Timeline}

/*
* Mailbox handles timing of message sending and heartbeat and emits corresponding events                                                                                                                   events
* */
class Mailbox(timeline:Timeline) extends MessageReceiveEventDispatcher with MessageSendEventDispatcher{

  private var messages:List[String] = List()
  private var initiated = false

  def get():List[String]  = messages

  def initiate() = {
    if(!initiated)
      scheduler.initiate()
  }

  private val scheduler:DatagramScheduler = {
    val sch = new DatagramScheduler(timeline)
    sch.addOnRetransmitEventListener({ e:DatagramScheduleEvent =>
      dispatchSendEvent()
    })
    sch.addOnHeartbeatEventListener({e:DatagramScheduleEvent =>
      dispatchHeartbeatEvent()
    })
    sch
  }

  def set(messages: List[String]): Unit = {
    if(!initiated)
      initiate()

    if(messages.isEmpty)
      scheduler.emptied()
    else
      scheduler.filled()

    this.messages = messages

    dispatchSendEvent()
  }

  private def dispatchSendEvent():Unit = {
    for(message <- messages)
      dispatchSendEvent(message)
  }

  def received():Unit = {
    if(!initiated)
      initiate()

    scheduler.received()
  }

  def receivedValid():Unit = {
    if(!initiated)
      initiate()

    scheduler.receivedValid()
  }

  def clear(): Unit = {
    if(!initiated)
      initiate()

    messages = List()
    scheduler.emptied()
  }

  def reestablish() = {
    if(messages.isEmpty)
      scheduler.reestablishedEmpty()
    else
      scheduler.reestablishedFilled()
  }
}
