package asynccom.connection

import com.kindone.asynccom.events.MessageReceiveEventDispatcher

trait Connection extends MessageReceiveEventDispatcher {
  def send(str: String): Unit
  def isOpen: Boolean
}
