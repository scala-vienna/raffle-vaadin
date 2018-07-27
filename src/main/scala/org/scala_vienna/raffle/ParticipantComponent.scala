package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.{Anchor, Label}
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.textfield.TextField
import org.vaadin.addons.vaactor.Vaactor

/** Handles interaction of a human participant with the system.
  *
  * Communicates exclusive with the session actor.
  */
class ParticipantComponent extends VerticalLayout
  with Vaactor.HasActor with Vaactor.SubscribeSession {

  /** Contains participant name */
  val participantName = new TextField("Name:")
  participantName.addValueChangeListener(_ => enterButton.click())
  participantName.focus()

  val participantNameLabel = new Label()
  participantNameLabel.setVisible(false)

  val enterButton = new Button("Enter Raffle", _ => session ! RaffleServer.Enter(participantName.getValue))

  val leaveButton = new Button("Leave Raffle", _ => session ! RaffleServer.Leave(""))
  leaveButton.setVisible(false)

  val enterPanel = new HorizontalLayout(
    participantName,
    participantNameLabel,
    enterButton,
    leaveButton
  )

  val participantsPanel = new NamePanel()

  val winnerLabel = new WinnerPanel()

  val footer = new Anchor("https://github.com/scala-vienna/vaadin-raffle", "Source code (GitHub)")

  add(
    enterPanel,
    participantsPanel,
    winnerLabel,
    footer
  )

  // MUST NOT access session or receive messages before attach!
  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    session ! Session.ReportState
    session ! Session.RequestRaffleState
  }

  override def receive: Receive = {
    case reply: RaffleServer.Reply => reply match {
      case state: RaffleServer.State =>
        winnerLabel.show(state.winner)
        participantsPanel.show(state.names)
      case RaffleServer.Entered(name) =>
        enterButton.setVisible(false)
        leaveButton.setVisible(true)
        participantName.setVisible(false)
        participantNameLabel.setText(s"Good luck, $name!")
        participantNameLabel.setVisible(true)
      case RaffleServer.Left(_) =>
        enterButton.setVisible(true)
        leaveButton.setVisible(false)
        participantName.setVisible(true)
        participantNameLabel.setVisible(false)
      case RaffleServer.Error(error) =>
        Notification.show(error)
      case RaffleServer.Terminated =>
        ConfirmDialog("Raffle terminated!").onOK(_ => ui.navigate("")).open()
    }
  }

}
