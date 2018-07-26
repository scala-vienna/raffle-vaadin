package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.{AttachEvent, DetachEvent}
import org.vaadin.addons.vaactor.{Vaactor, VaactorSession}

/** Handles interaction of a human administrator with the system.
  *
  * Communicates exclusive with the raffle actor.
  */
class AdminComponent(raffle: Manager.Raffle) extends VerticalLayout
  with Vaactor.HasActor {

  val participantsPanel = new NamePanel()

  val startButton = new Button("Start", _ => raffle ! RaffleServer.SelectWinner)

  val removeButton = new Button("Remove", _ => raffle ! RaffleServer.Leave(participantsPanel.getValue))

  val removeAllButton = new Button("Remove All", _ => raffle ! RaffleServer.Clear)

  val winnerLabel = new WinnerPanel()

  add(
    participantsPanel,
    winnerLabel,
    startButton,
    removeButton,
    removeAllButton,
  )

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    raffle ! VaactorSession.Subscribe
    raffle ! VaactorSession.RequestSessionState
  }

  override def onDetach(detachEvent: DetachEvent): Unit = {
    raffle ! VaactorSession.Unsubscribe
    super.onDetach(detachEvent)
  }

  override def receive: Receive = {
    case reply: RaffleServer.Reply => reply match {
      case state: RaffleServer.State =>
        winnerLabel.show(state.winner)
        participantsPanel.show(state.names)
        startButton.setEnabled(state.nonEmpty)
        removeButton.setEnabled(state.nonEmpty)
        removeAllButton.setEnabled(state.nonEmpty)
      case RaffleServer.Entered(_) =>
      case RaffleServer.Left(_) =>
      case RaffleServer.Error(error) =>
        Notification.show(error)
    }
  }

}
