package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
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

  val resetButton = new Button("Reset", _ => raffle ! RaffleServer.ClearWinner)

  val removeButton = new Button("Remove", _ => raffle ! RaffleServer.Leave(participantsPanel.getValue))

  val removeAllButton = new Button("Remove All", _ => raffle ! RaffleServer.Clear)

  val winnerLabel = new WinnerPanel()

  participantsPanel.addValueChangeListener { _ =>
    Option(participantsPanel.getValue) match {
      case Some(v) => removeButton.setEnabled(v.nonEmpty)
      case None => removeButton.setEnabled(false)
    }
  }

  add(new HorizontalLayout(
    new VerticalLayout(
      participantsPanel,
      winnerLabel
    ),
    new VerticalLayout(
      startButton,
      resetButton,
      removeButton,
      removeAllButton,
    )
  ))

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
        startButton.setEnabled(state.nonEmpty && state.winner.isEmpty)
        removeButton.setEnabled(false)
        resetButton.setEnabled(state.winner.nonEmpty)
        removeAllButton.setEnabled(state.nonEmpty)
      case RaffleServer.Entered(_) =>
      case RaffleServer.Left(_) =>
      case RaffleServer.Error(error) =>
        Notification.show(error)
      case RaffleServer.Terminated =>
        ui.navigate("")
    }
  }

}
