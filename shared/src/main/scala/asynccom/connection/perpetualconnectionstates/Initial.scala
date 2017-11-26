package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class Initial(val context: PerpetualConnectionStateContext) extends PerpetualConnectionState {
  def tryConnect(): Unit = {
    changeState(new Connecting(context))
    context.tryConnect()
  }

  def waitForOpenComplete(): Unit = unexpected

  def closed(): Unit = unexpected

  def failAndRetry(): Unit = unexpected

  def openTimedOut(): Unit = unexpected

  def succeed(): Unit = unexpected

  override def toString() = "Initial"
}
