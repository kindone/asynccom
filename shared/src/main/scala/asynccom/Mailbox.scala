package asynccom

import com.kindone.asynccom.events.MessageReceiveEventDispatcher

trait Mailbox extends MessageReceiveEventDispatcher {
  def setMailbox(messages: List[String]): Unit
  def clearMailbox(): Unit
}
