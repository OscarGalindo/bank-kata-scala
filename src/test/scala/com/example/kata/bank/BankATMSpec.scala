package com.example.kata.bank

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, Matchers, WordSpecLike}
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

class BankATMSpec(_system: ActorSystem)
  extends TestKit(_system)
    with Matchers
    with WordSpecLike
    with BeforeAndAfterAll
    with BeforeAndAfter {

  var testProbe: TestProbe = _
  var atm: ActorRef = _

  before {
    testProbe = TestProbe()
    atm = system.actorOf(ATM.props(testProbe.ref))
  }

  def this() = this(ActorSystem("BankATM"))

  override def afterAll: Unit = {
    shutdown(system)
  }

  implicit val timeout = Timeout(1 seconds)


  "Start the ATM by inserting the card" should {
    "tells the user the card needs a PIN" in {
      val result = atm ? InsertCard("4000-0000-0000-0000")

      blockingGet(result).shouldEqual(PinRequired())
    }

    "Welcomes the user after the pin is inserted" in {
      atm ! InsertCard("4000-0000-0000-0123")
      val result = atm ? TypePin("0123")

      blockingGet(result).shouldEqual(WelcomeMessage("Hello, John!"))
    }
    "Says wrong ping after the wrong pin is inserted" in {
      atm ! InsertCard("4000-0000-0000-0123")
      val result = atm ? TypePin("0000")

      blockingGet(result).shouldEqual(WrongPin())
    }
  }

  "No operation can be performed without the card" should {
    "deposit" in {
      val resultingMessage = atm ? Deposit(500)

      blockingGet(resultingMessage).shouldEqual(NotLoggedIn())
    }
  }

  "Deposit money on an account" should {
    "tells the user the operation was a success" in {
      atm ! InsertCard("4000-0000-0000-0123")
      atm ! TypePin("0123")

      val resultingMessage = atm ? Deposit(500)

      blockingGet(resultingMessage).shouldEqual(SuccessMessage("Deposited 500 EUR"))
    }
  }

  private def blockingGet(resultingMessage: Future[Any]) = {
    Await.result(resultingMessage, timeout.duration)
  }
}
