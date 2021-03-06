package com.example.kata.bank

import akka.actor.{Actor, ActorRef, Props}

class ATM(userIdentification: ActorRef, printer: ActorRef) extends Actor {
  import com.example.kata.bank.ATM._

  var card: String = _

  def receive: Receive = {
    case InsertCard(cardNumber) =>
      this.card = cardNumber
      context become ATMWithCard
  }

  def ATMWithCard: Receive = {
    case TypePin(pin) =>
      if (isValidPin(pin)) {
        printer ! WelcomeMessage("Hello, John!")
        context become AuthenticatedATM
      } else {
        printer ! WrongPin()
      }
  }

  def AuthenticatedATM: Receive = {
    case Deposit(amount) => printer ! DepositSuccess(amount)
  }

  def isValidPin(pin: String): Boolean = "-".r.split(card)(3) == pin
}

object ATM {
  def props(userIdentification: ActorRef, printer: ActorRef): Props = Props(new ATM(userIdentification, printer))

  case class DepositSuccess(amount: Int)
}
