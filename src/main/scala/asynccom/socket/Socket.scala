package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{ MessageReceiveEventDispatcher, SocketEventDispatcher }

/**
 * Created by kindone on 2016. 12. 5..
 */
object Socket {
  type Protocol = Int
}

trait Socket extends SocketEventDispatcher {
  def close(): Unit
  def send(str: String): Unit
}

trait PerpetualSocketTrait extends MessageReceiveEventDispatcher {
  def send(str: String): Unit
  def isAlive: Boolean
  def isOpen: Boolean
}

trait MailboxSocket extends MessageReceiveEventDispatcher {
  def setMailbox(messages: List[String]): Unit
  def clearMailbox(): Unit
}
