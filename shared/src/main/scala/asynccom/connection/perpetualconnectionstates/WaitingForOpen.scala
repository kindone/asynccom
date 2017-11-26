package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class WaitingForOpen(val context: PerpetualConnectionStateContext) extends PerpetualConnectionState {
  def tryConnect(): Unit = unexpected

  def waitForOpenComplete(): Unit = unexpected

  def closed(): Unit = unexpected

  def failAndRetry(): Unit = unexpected

  def openTimedOut(): Unit = {
    context.cancelOpenTimeout()
    changeState(new Connecting(context))
    context.tryConnect()
  }

  def succeed(): Unit = {
    context.cancelOpenTimeout()
    changeState(new Connected(context))
  }

  override def toString() = "WaitingForOpen"
}
