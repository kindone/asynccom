package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class Connected(val context: PerpetualConnectionStateContext) extends PerpetualConnectionState {
  def tryConnect(): Unit = unexpected

  def waitForOpenComplete(): Unit = unexpected

  def closed(): Unit = {
    changeState(new ConnectionClosed(context))
    context.scheduleReconnect(0)
  }

  def failAndRetry(): Unit = unexpected

  def openTimedOut(): Unit = unexpected

  def succeed(): Unit = unexpected

  override def toString() = "Connected"
}
