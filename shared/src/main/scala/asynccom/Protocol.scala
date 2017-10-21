package com.kindone.asynccom


/**
 * Created by kindone on 2016. 4. 23..
 */

sealed abstract class ClientToServerMessage {
  def reqId: Long
}

sealed trait ServerToClientMessage

case class Request(reqId:Long, str:String) extends ClientToServerMessage
case class Response(reqId: Long, status:Int, logId: Long, message: String) extends ServerToClientMessage
{
  def success:Boolean = status == 1
}
