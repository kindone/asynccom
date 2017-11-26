package asynccom.connection

import asynccom.connection.perpetualconnectionstates.PerpetualConnectionState

/**
 * Created by kindone on 2017. 2. 22..
 */

trait PerpetualConnectionStateContext {
  def changeState(newState: PerpetualConnectionState): Unit

  def tryConnect(): Unit

  def scheduleOpenTimeout(): Unit

  def scheduleReconnect(timeoutStep: Int): Unit

  def cancelOpenTimeout(): Unit

  def cancelReconnect(): Unit
}
