package org.scala_vienna.raffle

import akka.actor.Actor
import org.scala_vienna.raffle.RaffleServer._
import org.vaadin.addons.vaactor.VaactorSession

class Session extends Actor with VaactorSession[SessionState.State] {

  RaffleServer.raffleServer ! RegisterSession

  override val initialSessionState: SessionState.State = SessionState.None

  override val sessionBehaviour: Receive = {
    case newState: SessionState.State =>
      sessionState = newState
      broadcast(sessionState)
    case msg@(Winner(_) | Participants(_)) =>
      broadcast(msg)
  }

}
