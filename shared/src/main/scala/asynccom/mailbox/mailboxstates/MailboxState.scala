package com.kindone.asynccom.mailbox.mailboxstates

import com.kindone.asynccom.mailbox.MailboxStateContext

trait MailboxState {
  val context: MailboxStateContext

  def initiate(): Unit

  def filled(): Unit

  def emptied(): Unit

  def received(): Unit

  def receivedValid(): Unit

  def retransmit(): Unit

  def heartbeat(): Unit

  def fatalErrorOccurred(): Unit

  def unresponsive(): Unit

  def reestablishedEmpty(): Unit

  def reestablishedFilled(): Unit

  def changeState(newState: MailboxState): Unit = {
    context.changeState(newState)
  }

  def unexpected = {
    throw new UnsupportedOperationException("unexpected operation for MailboxState: " + this.toString)
  }

}