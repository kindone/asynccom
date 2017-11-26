package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class Connecting(val context: PerpetualConnectionStateContext) extends PerpetualConnectionState {
  def tryConnect(): Unit = unexpected

  def waitForOpenComplete(): Unit = {
    context.scheduleOpenTimeout()
    changeState(new WaitingForOpen(context))
  }

  def closed(): Unit = unexpected

  def failAndRetry(): Unit = {
    context.scheduleReconnect(0)
    changeState(new ConnectionRefused(context, 0))
  }

  def openTimedOut(): Unit = unexpected

  def succeed(): Unit = unexpected

  override def toString() = "Connecting"
}
