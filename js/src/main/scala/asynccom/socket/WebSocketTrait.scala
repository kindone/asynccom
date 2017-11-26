package com.kindone.asynccom.socket

import org.scalajs.dom.raw.{CloseEvent, ErrorEvent, Event, MessageEvent}

import scala.scalajs.js

trait WebSocketTrait {
  self:WebSocketLike =>

  def send(str: String): Unit
  def close(code: Int, reason: String): Unit
  var onopen: js.Function1[Event, _]
  var onerror: js.Function1[ErrorEvent, _]
  var onmessage: js.Function1[MessageEvent, _]
  var onclose: js.Function1[CloseEvent, _]
  def toString():String
}
