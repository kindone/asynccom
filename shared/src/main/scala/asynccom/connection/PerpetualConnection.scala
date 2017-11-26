package asynccom.connection

import asynccom.connection.perpetualconnectionstates._
import com.kindone.asynccom.events._
import com.kindone.asynccom.socket._
import com.kindone.timer.{SimulatedTimeline, Timeline, Timer}

object PerpetualConnection {
  val OPEN_TIMEOUT_MS = 25000
  val BACKOFF_BASE_MS = 500
}


class PerpetualConnection(val config:PerpetualConnectionConfig)
  extends Connection with PerpetualConnectionStateContext {

  import PerpetualConnection._

  private val retryTimer = new Timer(config.timeline)
  private var socket: Option[Socket] = None
  private var state: PerpetualConnectionState = new Initial(this)

  private def getState() = this.state

  def isOpen: Boolean = state.isInstanceOf[Connected]

  def stateName:String = state.toString

  def initiate():Unit = {
    getState().tryConnect()
  }

  def send(str: String): Unit = {
    socket.foreach(_.send(str))
  }

  def tryConnect(): Unit = {
    // expected state: Connecting
    val ws = config.socketFactory.create()
    ws.addOnReceiveListener(onReceive _)
    ws.addOnSocketOpenListener({ e: SocketOpenCloseEvent =>
      getState().succeed()
    })

    ws.addOnSocketCloseListener({ e: SocketOpenCloseEvent =>
      getState().closed()
    })

    try {
      ws.open(config.url)
      assert(ws.numSocketOpenEventListeners >= 1)
      setSocket(ws)
      getState().waitForOpenComplete()  // schedule open timeout & shift to WaitingForOpen state
    } catch {
      case err: RuntimeException =>
        //case err: JavaScriptException =>
        println("Exception occurred in creating WebSocket object: " + err.toString())
        getState().failAndRetry() // schedule reconnect & shift to ConnectionRefused state
    }
  }

  def scheduleOpenTimeout(): Unit = {
    retryTimer.setTimeout(OPEN_TIMEOUT_MS) {
      getState().openTimedOut()
    }
  }

  def cancelOpenTimeout(): Unit = {
    println("WebSocket canceled open timeout")
    retryTimer.clear()
  }

  override def scheduleReconnect(backOff: Int): Unit = {
    val saturatedBackOff = if (backOff < 10) backOff else 10
    val timeMs = (Math.pow(2.0, saturatedBackOff) * BACKOFF_BASE_MS).toLong
    retryTimer.setTimeout(timeMs) {
      getState().tryConnect()
    }
  }

  override def cancelReconnect(): Unit = {
    println("WebSocket canceled reconnect timeout")
    retryTimer.clear()
  }

  override def changeState(newState: PerpetualConnectionState): Unit = {
    state = newState
  }

  def getSocket() = socket

  private def setSocket(newSocket:Socket) = {
    clearSocket()
    socket = Some(newSocket)
  }

  private def clearSocket() = {
    socket.foreach(s => {
      s.removeAllOnReceiveListener()
      s.removeAllOnErrorListener()
      s.removeAllOnSocketOpenCloseListener()
    })
    socket = None
  }

  private def onReceive(e: MessageReceiveEvent) = {
    dispatchReceiveEvent(e.str)
  }

}



