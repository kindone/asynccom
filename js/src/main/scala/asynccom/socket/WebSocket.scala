package com.kindone.asynccom.socket

import org.scalajs.dom
import org.scalajs.dom.raw.{ ErrorEvent, Event, MessageEvent }
import scala.scalajs.js.|

/**
 * Created by kindone on 2016. 12. 6..
 */
class WebSocket(val ws: Any) extends Socket {

  val self = this
  val wsDynamic = ws.asInstanceOf[scalajs.js.Dynamic]
  wsDynamic.onopen = (e: Event) => self.onOpen(e)
  wsDynamic.onerror = (e: ErrorEvent) => self.onError(e)
  wsDynamic.onclose = (e: Event) => self.onClose(e)
  wsDynamic.onmessage = (e: MessageEvent) => self.onMessage(e)

  private def onMessage(evt: MessageEvent) = {
    println("WebSocket onMessage event called: " + evt.data)
    dispatchReceiveEvent(evt.data.toString)
  }

  private def onOpen(evt: Event) = {
    println("WebSocket onOpen event called: " + self + ":" + ws.toString + ":" + numSocketCloseEventListeners + ":" + evt.toString)
    dispatchSocketOpenEvent()
  }

  private def onClose(evt: Event) = {
    println("WebSocket onClose event called: " + evt.toString)
    dispatchSocketCloseEvent()
  }

  private def onError(evt: ErrorEvent) = {
    println("Error occurred in WebSocket: " + evt.toString + evt.message)
    dispatchErrorEvent(evt.message)
  }

  def close(): Unit = {
    println("WebSocket::close()")
    wsDynamic.close(1000, "normal")
    removeAllOnErrorListener()
    removeAllOnReceiveListener()
    removeAllOnSocketOpenCloseListener()
  }

  def send(str: String): Unit = {
    wsDynamic.send(str)
  }
}
