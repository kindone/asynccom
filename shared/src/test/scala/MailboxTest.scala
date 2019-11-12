package com.kindone.asynccom.mailbox

import com.kindone.asynccom.events.MessageSendEvent
import com.kindone.asynccom.mailbox.mailboxstates.Empty
import com.kindone.event._
import com.kindone.timer.{SimulatedTimeline, Timeline}
import org.scalatest._
import org.scalamock.scalatest.MockFactory

import scala.collection.immutable.List


class MailboxTest extends FlatSpec with MockFactory with Matchers with GivenWhenThen {

  "DatagramScheduler" should "work as expected in empty states" in {

    When("Scheduler is created")
    val timeline = new SimulatedTimeline
    val scheduler = new DatagramScheduler(timeline)

    Then("Scheduler state is initial")
    scheduler.state.toString shouldEqual ("Initial")

    When("Scheduler is initiated")
    scheduler.initiate()

    Then("Scheduler state is Empty and a heartbeat timer is scheduled")
    scheduler.state.toString shouldEqual ("Empty(0)")
    timeline.scheduledEntries.size shouldEqual (1)
    timeline.canceledEntries.size shouldEqual (0)

    for(i <- 1 to DatagramScheduler.MAX_HEARTBEAT_TRIAL)
    {
      When(s"Time is advanced by Heartbeat interval(${DatagramScheduler.HEARTBEAT_INTERVAL_MS}, step:$i)")
      timeline.advance(DatagramScheduler.HEARTBEAT_INTERVAL_MS)

      Then("Heartbeat timer is fired and rescheduled")
      timeline.firedEntries.size shouldEqual (i)
      timeline.scheduledEntries.size shouldEqual (1)
      timeline.canceledEntries.size shouldEqual (0)
    }

    When(s"Time is advanced by Heartbeat interval(${DatagramScheduler.HEARTBEAT_INTERVAL_MS}, step:${DatagramScheduler.MAX_HEARTBEAT_TRIAL+1})")
    timeline.advance(DatagramScheduler.HEARTBEAT_INTERVAL_MS)
    Then("last Heartbeat timer is fired and state is set to unresponsive")
    timeline.firedEntries.size shouldEqual(DatagramScheduler.MAX_HEARTBEAT_TRIAL+1)
    timeline.scheduledEntries.size shouldEqual(0)
    timeline.canceledEntries.size shouldEqual(0)
    scheduler.state.toString shouldEqual("Unresponsive")

    When("scheduler is re-established")
    scheduler.reestablishedEmpty()

    Then("Scheduler state is Empty and a heartbeat timer is scheduled")
    scheduler.state.toString shouldEqual("Empty(0)")
    timeline.scheduledEntries.size shouldEqual(1)
    timeline.canceledEntries.size shouldEqual(0)
  }

  "DatagramScheduler" should "work as expected in filled states" in {

    val timeline = new SimulatedTimeline
    val scheduler = new DatagramScheduler(timeline)
    scheduler.initiate()

    When("datagram is filled")
    scheduler.filled()

    Then("Scheduler state is Filled and a retransmit timer is scheduled, cancelling heartbeat timer")
    scheduler.state.toString shouldEqual("Filled(0)")
    timeline.scheduledEntries.size shouldEqual(1)
    timeline.canceledEntries.size shouldEqual(1)

    for(i <- 1 to DatagramScheduler.MAX_RETRANSMIT_TRIAL)
    {
      When(s"Time is advanced by Retransmit interval(${DatagramScheduler.RETRANSMIT_INTERVAL_MS}, step:$i)")
      timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS)

      Then("Retransmit timer is fired and rescheduled")
      timeline.firedEntries.size shouldEqual (i)
      timeline.scheduledEntries.size shouldEqual (1)
      timeline.canceledEntries.size shouldEqual (1)
    }

    When(s"Time is advanced by Retransmit interval(${DatagramScheduler.RETRANSMIT_INTERVAL_MS}, step:${DatagramScheduler.MAX_RETRANSMIT_TRIAL+1})")
    timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS)
    Then("last Retransmit timer is fired and state is set to unresponsive")
    timeline.firedEntries.size shouldEqual(DatagramScheduler.MAX_RETRANSMIT_TRIAL+1)
    timeline.scheduledEntries.size shouldEqual(0)
    timeline.canceledEntries.size shouldEqual(1)
    scheduler.state.toString shouldEqual("Unresponsive")

    When("scheduler is re-established")
    scheduler.reestablishedFilled()

    Then("Scheduler state is Filled and a retransmit timer is scheduled")
    scheduler.state.toString shouldEqual("Filled(0)")
    timeline.scheduledEntries.size shouldEqual(1)
    timeline.canceledEntries.size shouldEqual(1)

    When("scheduler receives valid packet before retransmit timer fires")
    timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS/2)
    scheduler.receivedValid()

    Then("Scheduler resets timer")
    timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS/2)
    scheduler.state.toString shouldEqual("Filled(0)")
    timeline.firedEntries.size shouldEqual(DatagramScheduler.MAX_RETRANSMIT_TRIAL+1)
    timeline.scheduledEntries.size shouldEqual(1)
    timeline.canceledEntries.size shouldEqual(2)

  }


  "Mailbox" should "work as expected" in {
    val timeline = new SimulatedTimeline
    val mailbox = new Mailbox(timeline)

    mailbox.get().isEmpty should be(true)

    mailbox.addOnSendListener({ e:MessageSendEvent =>
      println(s"message sent: '${e.str}'")
    })

    mailbox.initiate()

    mailbox.set(List("hello", "world"))

    timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS)

    mailbox.receivedValid()

    timeline.advance(DatagramScheduler.RETRANSMIT_INTERVAL_MS)



  }
}