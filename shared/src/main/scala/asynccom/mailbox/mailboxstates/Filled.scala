package com.kindone.asynccom.mailbox.mailboxstates
import com.kindone.asynccom.mailbox.MailboxStateContext

class Filled(val context:MailboxStateContext, val retryCount:Int = 0) extends MailboxState {

  override def initiate(): Unit = unexpected

  override def filled(): Unit = {
    // DO nothing
  }

  override def emptied(): Unit = {
    context.cancelTimer()
    context.changeState(new Empty(context))
    context.scheduleHeartbeat(0)
  }

  override def received(): Unit = {
    // DO nothing
  }

  override def receivedValid(): Unit = {
    context.changeState(new Filled(context, 0))
    context.scheduleRetransmit(0)
  }

  override def fatalErrorOccurred(): Unit = {
    context.cancelTimer()
    context.changeState(new Fatal(context))
    context.dispatchFatalErrorEvent()
  }

  override def unresponsive(): Unit = {
    context.cancelTimer()
    context.changeState(new Unresponsive(context))
    context.dispatchUnresponsiveEvent()
  }

  override def retransmit(): Unit = {
    context.changeState(new Filled(context, retryCount + 1))
    context.scheduleRetransmit(retryCount + 1)
    context.dispatchRetransmitEvent()
  }

  override def heartbeat(): Unit = unexpected

  override def reestablishedEmpty(): Unit = unexpected

  override def reestablishedFilled(): Unit = unexpected

  override def toString() = s"Filled(${retryCount})"
}
