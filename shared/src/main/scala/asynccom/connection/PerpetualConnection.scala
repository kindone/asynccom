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
  extends Connection {

  import PerpetualConnection._


  private val scheduleTimer = new Timer(config.timeline)

  private var socketOpt: Option[Socket] = None

  private var connState: PerpetualConnectionState = new Initial(handle)

  private[connection] def getState() = this.connState

  private var initiated = false

  def isOpen: Boolean = connState.isInstanceOf[Connected]

  def stateName:String = connState.toString


  def initiate():Unit = {
    if(!initiated) {
      getState().tryConnect()
      initiated = true
    }
  }

  def send(str: String): Unit = {
    socketOpt.foreach(_.send(str))
  }

  private object handle extends PerpetualConnectionStateContext
  {
    def tryConnect(): Unit = {
      // expected state: Connecting
      val ws = createSocket()

      try {
        ws.open(config.url)
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
      scheduleTimer.setTimeout(OPEN_TIMEOUT_MS) {
        getState().openTimedOut()
      }
    }

    def cancelOpenTimeout(): Unit = {
      println("WebSocket canceled open timeout")
      scheduleTimer.clear()
    }

    def scheduleReconnect(backOff: Int): Unit = {
      val saturatedBackOff = if (backOff < 10) backOff else 10
      val timeMs = (Math.pow(2.0, saturatedBackOff) * BACKOFF_BASE_MS).toLong
      scheduleTimer.setTimeout(timeMs) {
        getState().tryConnect()
      }
    }

    def cancelReconnect(): Unit = {
      println("WebSocket canceled reconnect timeout")
      scheduleTimer.clear()
    }

    def changeState(newState: PerpetualConnectionState): Unit = {
      connState = newState
    }
  }

  def getSocket() = socketOpt

  private def createSocket():Socket = {
    val socket = config.socketFactory.create()
    socket.addOnReceiveListener(onReceive _)
    socket.addOnSocketOpenListener({ e: SocketOpenCloseEvent =>
      getState().succeed()
    })

    socket.addOnSocketCloseListener({ e: SocketOpenCloseEvent =>
      getState().closed()
    })
    socket
  }

  private def setSocket(newSocket:Socket) = {
    clearSocket()
    socketOpt = Some(newSocket)
  }

  private def clearSocket() = {
    socketOpt.foreach(s => {
      s.removeAllOnReceiveListener()
      s.removeAllOnErrorListener()
      s.removeAllOnSocketOpenCloseListener()
    })
    socketOpt = None
  }

  private def onReceive(e: MessageReceiveEvent) = {
    dispatchReceiveEvent(e.str)
  }

}



