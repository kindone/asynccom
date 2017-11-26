package com.kindone.asynccom.socket

import org.scalajs.dom
import org.scalajs.dom.raw.{ ErrorEvent, Event, MessageEvent }
import scala.scalajs.js.|

/**
 * Created by kindone on 2016. 12. 6..
 */
class WebSocket(private var inSocket:Option[WebSocketLike] = None) extends Socket {

  val self = this

  // bind ws if given
  inSocket = inSocket.map { ws =>
    bind(ws)
  }

  def open(url:String): Unit = {
    if(inSocket.isDefined)
      throw new UnsupportedOperationException("WebSocket is already open")

    val domWebSocket = new org.scalajs.dom.raw.WebSocket(url)
    inSocket = Some(bind(domWebSocket))
  }

  def close(): Unit = {
    println("WebSocket::close()")
    inSocket.foreach(_.close(1000, "normal"))
    removeAllOnErrorListener()
    removeAllOnReceiveListener()
    removeAllOnSocketOpenCloseListener()
    inSocket = None
  }

  def send(str: String): Unit = {
    inSocket.foreach(_.send(str))
  }

  private def bind(inSocket:WebSocketLike) = {
    inSocket.onopen = (e: Event) => self.onOpen(e)
    inSocket.onerror = (e: ErrorEvent) => self.onError(e)
    inSocket.onclose = (e: Event) => self.onClose(e)
    inSocket.onmessage = (e: MessageEvent) => self.onMessage(e)
    inSocket
  }

  private def onMessage(evt: MessageEvent) = {
    println("WebSocket onMessage event called: " + evt.data)
    dispatchReceiveEvent(evt.data.toString)
  }

  private def onOpen(evt: Event) = {
    println("WebSocket onOpen event called: " + self + ":" + inSocket.toString + ":" + numSocketCloseEventListeners + ":" + evt.toString)
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
}
