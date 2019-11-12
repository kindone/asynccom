package asynccom.client

import com.kindone.asynccom.mailbox.Mailbox
import asynccom.connection.{Connection, PerpetualConnection, PerpetualConnectionConfig}
import com.kindone.asynccom.events.MessageReceiveEventDispatcher

/* Responsibility:
    + contain connection to server
    + mailbox and heartbeat
  */
class Client(config:PerpetualConnectionConfig) extends MessageReceiveEventDispatcher{
  val conn:Connection = new PerpetualConnection(config)
//  val mailbox:Mailbox = new Mailbox {}

  def initiate() = conn.initiate()

  def setMailbox(messages:List[String]) = {

  }
}
