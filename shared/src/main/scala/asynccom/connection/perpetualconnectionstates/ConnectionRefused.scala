package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

class ConnectionRefused(val context: PerpetualConnectionStateContext, numRetry: Int) extends PerpetualConnectionState {
  def tryConnect(): Unit = {
    changeState(new ConnectionRetrying(context, numRetry))
    context.tryConnect()
  }

  def waitForOpenComplete(): Unit = unexpected

  def closed(): Unit = unexpected

  def failAndRetry(): Unit = unexpected

  def openTimedOut(): Unit = unexpected

  def succeed(): Unit = unexpected

  override def toString() = s"ConnectionRefused($numRetry)"
}
