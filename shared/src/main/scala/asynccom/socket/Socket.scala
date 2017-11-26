package com.kindone.asynccom.socket

import com.kindone.asynccom.events.{ MessageReceiveEventDispatcher, SocketEventDispatcher }

/**
 * Created by kindone on 2016. 12. 5..
 */
object Socket {
  type Protocol = Int
}

trait Socket extends SocketEventDispatcher {
  def open(url:String):Unit
  def close(): Unit
  def send(str: String): Unit
}




