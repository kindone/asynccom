package com.kindone.asynccom

import asynccom.connection.{PerpetualConnection, PerpetualConnectionConfig}
import com.kindone.asynccom.socket.{Socket, SocketFactory}
import com.kindone.event._
import com.kindone.timer.SimulatedTimeline
import org.scalatest._
import org.scalamock.scalatest.MockFactory

class MacWireTest extends FlatSpec with MockFactory with Matchers {

  "Macwire" should "work" in {

    class DatabaseAccess()
    class SecurityFilter()
    class UserFinder(databaseAccess: DatabaseAccess, securityFilter: SecurityFilter)
    class UserStatusReader(userFinder: UserFinder, val factor:Double)


    trait UserModule {
      import com.softwaremill.macwire._

      lazy val theDatabaseAccess   = wire[DatabaseAccess]
      lazy val theSecurityFilter   = wire[SecurityFilter]
      lazy val theUserFinder       = wire[UserFinder]
      lazy val theUserStatusReader  = {
        val factor:Double = 1.0
        wire[UserStatusReader]
      }
    }

    val module = new UserModule {
    }

    module.theUserStatusReader
  }

  "Macwire" should "work2" in {

    class GenericModule(baseUrl:String) {
      import com.softwaremill.macwire._

      lazy val timeline = wire[SimulatedTimeline]
      lazy val socketFactory = new SocketFactory {
        override def create(): Socket = new Socket {
          override def open(url: String): Unit = {}

          override def close(): Unit = {}

          override def send(str: String): Unit = {}
        }
      }
      lazy val connectionConfig = wire[PerpetualConnectionConfig]
      lazy val perpetualSocket = wire[PerpetualConnection]
    }

    val module = new GenericModule("")
    module.perpetualSocket
  }
}
