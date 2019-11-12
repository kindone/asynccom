package com.kindone.asynccom.mailbox.mailboxstates
import com.kindone.asynccom.mailbox.MailboxStateContext

class Empty(val context:MailboxStateContext, retryCount:Int = 0) extends MailboxState {

  override def initiate(): Unit = unexpected

  override def filled(): Unit = {
    context.changeState(new Filled(context))
    context.scheduleRetransmit(0)
  }

  override def emptied(): Unit = {
    // DO nothing
  }

  override def received(): Unit = {
    context.scheduleHeartbeat(0)
  }

  override def receivedValid(): Unit = {
    context.scheduleHeartbeat(0)
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

  override def retransmit(): Unit = unexpected

  override def heartbeat(): Unit = {
    context.changeState(new Empty(context, retryCount + 1))
    context.scheduleHeartbeat(retryCount + 1)
    context.dispatchHeartbeatEvent()
  }

  override def reestablishedEmpty(): Unit = unexpected

  override def reestablishedFilled(): Unit = unexpected

  override def toString() = s"Empty(${retryCount})"
}
