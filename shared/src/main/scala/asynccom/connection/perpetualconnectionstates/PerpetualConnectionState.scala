package asynccom.connection.perpetualconnectionstates

import asynccom.connection.PerpetualConnectionStateContext

trait PerpetualConnectionState {
  val context: PerpetualConnectionStateContext

  def tryConnect(): Unit

  def waitForOpenComplete(): Unit

  def closed(): Unit

  def failAndRetry(): Unit

  def openTimedOut(): Unit

  def succeed(): Unit

  def changeState(newState: PerpetualConnectionState): Unit = {
    context.changeState(newState)
  }

  def unexpected = {
    throw new UnsupportedOperationException("unexpected operation for PerpetualConnectionState: " + this.toString)
  }

}