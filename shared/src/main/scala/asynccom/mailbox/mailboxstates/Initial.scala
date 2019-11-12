package com.kindone.asynccom.mailbox.mailboxstates

import com.kindone.asynccom.mailbox.MailboxStateContext


class Initial(val context:MailboxStateContext) extends MailboxState {

  override def initiate(): Unit = {
    changeState(new Empty(context))
    context.scheduleHeartbeat(0)
  }

  override def filled(): Unit = unexpected

  override def emptied(): Unit = unexpected

  override def received(): Unit = unexpected

  override def receivedValid(): Unit = unexpected

  override def fatalErrorOccurred(): Unit = unexpected

  override def unresponsive(): Unit = unexpected

  override def retransmit(): Unit = unexpected

  override def heartbeat(): Unit = unexpected

  override def reestablishedEmpty(): Unit = unexpected

  override def reestablishedFilled(): Unit = unexpected

  override def toString() = "Initial"
}
