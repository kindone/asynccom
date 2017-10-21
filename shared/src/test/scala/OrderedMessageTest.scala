package com.kindone.asynccom

import com.kindone.event._
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class OrderedMessageTest extends FlatSpec with MockFactory with Matchers {

  "unordered messages " should "be buffered until sequence is available" in {
    val mapbuf: MapBasedBuffer = new MapBasedBuffer
    val listbuf: ListBasedBuffer = new ListBasedBuffer

    for (buf <- List(mapbuf, listbuf)) {
      val msgproc = new OrderedMessage(buf)
      msgproc.request(Request(0, "hello"))
      msgproc.request(Request(1, "world"))
      msgproc.request(Request(2, "a"))
      msgproc.request(Request(3, "b"))
      msgproc.request(Request(4, "c"))
      msgproc.request(Request(5, "sdf"))

      msgproc.pendingRequests should be(
        List(
          (0, Request(0, "hello")),
          (1, Request(1, "world")),
          (2, Request(2, "a")),
          (3, Request(3, "b")),
          (4, Request(4, "c")),
          (5, Request(5, "sdf"))))

      msgproc.respond(Response(4, 0, 0, "hello")) should be(List())

      msgproc.respond(Response(1, 1, 0, "world")) should be(List())

      msgproc.respond(Response(3, 1, 0, "a")) should be(List())

      msgproc.respond(Response(0, 1, 0, "b")) should be(List(Response(0, 1, 0, "b"), Response(1, 1, 0, "world")))

      msgproc.respond(Response(2, 1, 0, "c")) should be(List(Response(2, 1, 0, "c"), Response(3, 1, 0, "a")))

    }

  }

}
