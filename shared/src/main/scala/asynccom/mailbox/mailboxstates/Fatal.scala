package com.kindone.asynccom.mailbox.mailboxstates

import com.kindone.asynccom.mailbox.MailboxStateContext


class Fatal(val context:MailboxStateContext) extends MailboxState {

  override def initiate(): Unit = unexpected

  override def filled(): Unit = unexpected

  override def emptied(): Unit = unexpected

  override def received(): Unit = {
    // DO nothing
  }

  override def receivedValid(): Unit = {
    // DO nothing
  }

  override def fatalErrorOccurred(): Unit = unexpected

  override def unresponsive(): Unit = unexpected

  override def retransmit(): Unit = unexpected

  override def heartbeat(): Unit = unexpected

  override def reestablishedEmpty(): Unit = {
    changeState(new Empty(context))
  }

  override def reestablishedFilled(): Unit = {
    changeState(new Filled(context))
  }

  override def toString() = "Fatal"
}
