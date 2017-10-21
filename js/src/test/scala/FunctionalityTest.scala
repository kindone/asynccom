package com.kindone.asynccom

import org.scalatest.{ Matchers, FunSuite }

class MessageProcessorTest extends FunSuite with org.scalamock.scalatest.MockFactory with Matchers {

  test("testSend") {

    /*
    val mockSocket = mock[MailboxSocket]
    inSequence {
      (mockSocket.addOnReceiveListener _).expects(*)
      (mockSocket.setMailbox _).expects(*)
    }
    val processor = new MessageProcessor(branch, mockSocket)
    processor.send[Boolean](ChangePanAction(0, 10.0, 0.0))
    */
  }


}
