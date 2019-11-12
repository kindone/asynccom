package com.kindone.asynccom.mailbox

import com.kindone.asynccom.mailbox.mailboxstates.{Initial, MailboxState}
import com.kindone.timer.{Timeline, Timer}

object DatagramScheduler
{
  val UNRESPONSIVE_TIMEOUT_MS = 25000
  val RETRANSMIT_INTERVAL_MS = 500
  val HEARTBEAT_INTERVAL_MS = 30000
  val BACKOFF_BASE_MS = 500
  val MAX_RETRANSMIT_TRIAL = 10
  val MAX_HEARTBEAT_TRIAL = 5
}


class DatagramScheduler(timeline:Timeline) extends DatagramSchedulerEventDispatcher {
  import DatagramScheduler._

  private val self = this

  var state:MailboxState = new Initial(handle)
  val timer = new Timer(timeline)

  def initiate() = {
    state.initiate()
  }

  def filled() = {
    state.filled()
  }

  def emptied() = {
    state.emptied()
  }

  def received() = {
    state.received()
  }

  def receivedValid() = {
    state.receivedValid()
  }

  def reestablishedEmpty() = {
    state.reestablishedEmpty()
  }

  def reestablishedFilled() = {
    state.reestablishedFilled()
  }

  private object handle extends MailboxStateContext
  {
    override def changeState(newState: MailboxState): Unit = {
      state = newState
    }

    override def scheduleRetransmit(numTried:Int): Unit = {
      if(numTried < MAX_RETRANSMIT_TRIAL)
      {
        timer.setTimeout(RETRANSMIT_INTERVAL_MS) {
          state.retransmit()
        }
      }
      else
      {
        timer.setTimeout(RETRANSMIT_INTERVAL_MS) {
          state.unresponsive()
        }
      }
    }

    override def scheduleHeartbeat(numTried:Int): Unit = {
      if(numTried < MAX_HEARTBEAT_TRIAL)
      {
        timer.setTimeout(HEARTBEAT_INTERVAL_MS) {
          state.heartbeat()
        }
      }
      else
      {
        timer.setTimeout(HEARTBEAT_INTERVAL_MS) {
          state.unresponsive()
        }
      }
    }

    override def cancelTimer(): Unit = {
      timer.clear()
    }

    override def dispatchRetransmitEvent(): Unit = self.dispatchRetransmitEvent()

    override def dispatchHeartbeatEvent(): Unit = self.dispatchHeartbeatEvent()

    override def dispatchFatalErrorEvent(): Unit = self.dispatchFatalErrorEvent()

    override def dispatchUnresponsiveEvent(): Unit = self.dispatchUnresponsiveEvent()
  }

}