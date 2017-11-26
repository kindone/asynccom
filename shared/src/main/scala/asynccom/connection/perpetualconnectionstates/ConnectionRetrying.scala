package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class ConnectionRetrying(val context: PerpetualConnectionStateContext, numRetry: Int) extends PerpetualConnectionState {
  def tryConnect(): Unit = unexpected

  def waitForOpenComplete(): Unit = {
    context.scheduleOpenTimeout()
    changeState(new WaitingForOpen(context))
  }

  def closed(): Unit = unexpected

  def failAndRetry(): Unit = {
    context.scheduleReconnect(numRetry)
    changeState(new ConnectionRefused(context, numRetry + 1))
  }

  def openTimedOut(): Unit = unexpected

  def succeed(): Unit = unexpected

  override def toString() = s"ConnectionRetrying($numRetry)"
}
