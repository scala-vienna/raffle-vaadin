package org.scala_vienna.raffle

import akka.actor.{Actor, ActorRef}
import org.scala_vienna.raffle.RaffleServer._
import org.scala_vienna.raffle.Session.RegisterUI
import org.vaadin.addons.vaactor.VaactorSession

object Session {

  case object RegisterUI

}

class Session extends Actor with VaactorSession[SessionState.State] {

  //noinspection ActorMutableStateInspection
  // List of UIs of this session
  private var uis = Set.empty[ActorRef]

  RaffleServer.raffleServer ! RegisterSession

  override val initialSessionState: SessionState.State = SessionState.None

  override val sessionBehaviour: Receive = {
    case newState: SessionState.State =>
      sessionState = newState
      broadcast(sessionState)
    case msg@(Winner(_) | Participants(_)) =>
      broadcast(msg)
    case RegisterUI =>
      uis += sender
      sender ! sessionState
      RaffleServer.raffleServer ! GetParticipants(sender)
      RaffleServer.raffleServer ! GetWinner(sender)
  }

  /** Send message to every UI
    *
    * @param msg message
    */
  def broadcast(msg: Any): Unit = uis foreach {
    _ ! msg
  }
}
