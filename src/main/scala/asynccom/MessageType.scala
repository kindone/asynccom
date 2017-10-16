package com.kindone.asynccom

sealed trait Message {
  def URI: String
  def clientId: Long
}

case class ClientMessage(URI: String, clientId: Long) extends Message {
}

