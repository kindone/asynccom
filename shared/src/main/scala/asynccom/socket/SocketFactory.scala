package com.kindone.asynccom.socket

/**
 * Created by kindone on 2017. 2. 12..
 */

trait SocketFactory {
  def create(): Socket
}

