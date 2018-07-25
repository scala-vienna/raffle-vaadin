package org.scala_vienna.raffle

import akka.actor.Actor
import org.scala_vienna.raffle.RaffleServer._
import org.vaadin.addons.vaactor.VaactorSession

class Session extends Actor with VaactorSession[SessionState.State] {

  private var raffle: Option[Manager.Raffle] = None

  override val initialSessionState: SessionState.State = SessionState.None

  override val sessionBehaviour: Receive = {
    case newState: SessionState.State =>
      sessionState = newState
      broadcast(sessionState)
    case msg@(Winner(_) | Participants(_)) =>
      broadcast(msg)
    case r: Manager.Raffle =>
      raffle = Some(r)
      r ! SubscribeSession
  }

  override def postStop(): Unit = for (r <- raffle) r ! UnsubscribeSession

}
