package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.{Anchor, H2, Label}
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.{DataProvider, ListDataProvider}
import org.vaadin.addons.vaactor.Vaactor

import scala.collection.JavaConverters._

class ParticipantComponent extends VerticalLayout
  with Vaactor.HasActor with Vaactor.SubscribeSession {

  /** Contains list of raffle participants */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)

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

  val participantsPanel = new ListBox[String]()
  participantsPanel.setDataProvider(participantsDataProvider)

  val startButton = new Button("Start", _ => session ! RaffleServer.SelectWinner)
  startButton.setVisible(false)
  startButton.setEnabled(false)

  val removeButton = new Button("Remove", _ => session ! RaffleServer.Leave(participantsPanel.getValue))
  removeButton.setVisible(false)
  removeButton.setEnabled(false)

  val removeAllButton = new Button("Remove All", _ => session ! RaffleServer.Clear)
  removeAllButton.setVisible(false)
  removeAllButton.setEnabled(false)

  val winnerCaption = "Winner:"
  val noWinnerCaption: String = winnerCaption + " -"

  val winnerLabel = new H2(noWinnerCaption)

  val footer = new Anchor("https://github.com/scala-vienna/vaadin-raffle", "Source code (GitHub)")

  add(
    enterPanel,
    participantsPanel,
    startButton,
    removeButton,
    removeAllButton,
    winnerLabel,
    footer
  )


  // MUST NOT access session or receive messages before attach!
  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    session ! Session.ReportState
    session ! Session.RequestRaffleState
  }

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: Receive = {
    case reply: RaffleServer.Reply => reply match {
      case state@RaffleServer.State(participants, winner) =>
        if (winner.isDefined)
          winnerLabel.setText(s"$winnerCaption ${winner.get}")
        else
          winnerLabel.setText(noWinnerCaption)
        participantsList.clear()
        participantsList.addAll(state.names.asJava)
        participantsDataProvider.refreshAll()
        startButton.setEnabled(participants.nonEmpty)
        removeButton.setEnabled(participants.nonEmpty)
        removeAllButton.setEnabled(participants.nonEmpty)
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
        startButton.setVisible(false)
        removeButton.setVisible(false)
        removeAllButton.setVisible(false)
      case RaffleServer.Error(error) =>
        Notification.show(error)
    }
  }

}
