package com.kindone.asynccom

/**
 * Created by kindone on 2017. 5. 7..
 */

trait OrderedMessageBuffer {
  def insert(msg: ClientToServerMessage): Unit

  def clearFailed(fromId: Long): Unit

  def markSuccessful(id: Long, response: Response): Unit

  def clearSucceeded(): List[Response]

  def pending: List[(Long, ClientToServerMessage)]

}

class ListBasedBuffer extends OrderedMessageBuffer {
  var sent: List[(Long, Boolean, ClientToServerMessage, Option[Response])] = List.empty

  def insert(req: ClientToServerMessage) = {
    sent :+= (req.reqId, false, req, None)
  }

  def clearFailed(fromId: Long) = {
    sent = sent filter {
      case (id, _, _, _) =>
        id < fromId
    }
  }

  def markSuccessful(targetId: Long, response: Response) = {
    sent = sent.map {
      case (id, success, request, responseOpt) =>
        if (id == targetId)
          (id, true, request, Some(response))
        else
          (id, success, request, responseOpt)
    }
  }

  def clearSucceeded(): List[Response] = {

    var index = sent.indexWhere {
      case (id, success, _, _) =>
        !success
    }
    index = if (index == -1) sent.length else index
    val (cleared, remained) = sent.splitAt(index)
    sent = remained
    cleared.map(_._4.get) // FIXME: option get can be dangerous
  }

  def pending: List[(Long, ClientToServerMessage)] = {
    sent.map {
      case (id, _, request, _) =>
        (id, request)
    }
  }

  override def toString = sent.toString
}

class MapBasedBuffer extends OrderedMessageBuffer {
  var sent: Map[Long, (Boolean, ClientToServerMessage, Option[Response])] = Map.empty

  def insert(req: ClientToServerMessage) = {
    sent += (req.reqId -> (false, req, None))
  }

  def clearFailed(fromId: Long) = {
    sent = sent filter {
      case (id, _) =>
        id < fromId
    }
  }

  def markSuccessful(id: Long, response: Response) = {
    sent.get(id).foreach {
      case (success, req, packet) =>
        if (!success)
          sent += (id -> (true, req, Some(response)))
    }
  }

  def clearSucceeded(): List[Response] = {
    val sortedList = sent.toList.sortBy { case (a, b) => a }
      .takeWhile {
        case (id, (success, _, _)) =>
          success
      }

    var succeeded: List[Response] = List()

    sortedList.foreach {
      case (id, (_, _, res)) =>
        sent -= id
        succeeded :+= res.get
    }
    succeeded
  }

  def pending: List[(Long, ClientToServerMessage)] = {
    val sortedList = sent.toList.sortBy { case (a, b) => a }

    sortedList.map {
      case (id, (_, request, _)) =>
        (id, request)
    }
  }

  override def toString = sent.toString
}

class OrderedMessage(buffer: OrderedMessageBuffer) {

  def request(req: ClientToServerMessage) = {
    buffer.insert(req)
  }

  def respond(response: Response): List[Response] = {
    if (response.success) {
      buffer.markSuccessful(response.reqId, response)
      val succeeded = buffer.clearSucceeded()
      succeeded
    } else {
      val failedId = response.reqId
      buffer.clearFailed(failedId)
      List.empty
    }
  }

  def pendingRequests = buffer.pending

  override def toString = buffer.toString
}
