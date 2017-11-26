package asynccom.connection

import com.kindone.asynccom.socket.SocketFactory
import com.kindone.timer.Timeline

class PerpetualConnectionConfig(val url:String, val socketFactory:SocketFactory, val timeline:Timeline) {
}
