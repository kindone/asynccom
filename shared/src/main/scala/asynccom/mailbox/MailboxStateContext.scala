package com.kindone.asynccom.mailbox

import com.kindone.asynccom.mailbox.mailboxstates._

/**
 * Created by kindone on 2017. 2. 22..
 */

trait MailboxStateContext {
  def changeState(newState: MailboxState): Unit

  def scheduleRetransmit(numTried:Int): Unit

  def scheduleHeartbeat(numTried:Int): Unit

  def cancelTimer(): Unit

  def dispatchRetransmitEvent():Unit

  def dispatchHeartbeatEvent():Unit

  def dispatchFatalErrorEvent(): Unit

  def dispatchUnresponsiveEvent (): Unit

}
