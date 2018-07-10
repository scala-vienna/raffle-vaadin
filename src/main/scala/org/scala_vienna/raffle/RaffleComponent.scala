package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.{Anchor, H1, H2, Label}
import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.{DataProvider, ListDataProvider}
import org.scala_vienna.raffle.RaffleServer._
import org.vaadin.addons.vaactor.{Vaactor, VaactorSession}

import scala.collection.JavaConverters._

class RaffleComponent(title: String)
  extends VerticalLayout with Vaactor.HasActor with Vaactor.SubscribeSession {

  /** Contains list of raffle participants */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)

  /** Contains participant name */
  val participantName = new TextField("Name:")
  participantName.addValueChangeListener(_ => enterButton.click())
  participantName.focus()

  val participantNameLabel = new Label()
  participantNameLabel.setVisible(false)

  val enterButton = new Button("Enter Raffle", _ =>
    if (participantName.getValue == "raffle2018coord")
      RaffleServer.raffleServer ! Coordinate(session)
    else
      RaffleServer.raffleServer ! Participate(participantName.getValue, session)
  )

  val leaveButton = new Button("Leave Raffle", _ => RaffleServer.raffleServer ! Leave(session))
  leaveButton.setVisible(false)

  val enterPanel = new HorizontalLayout(
    participantName,
    participantNameLabel,
    enterButton,
    leaveButton
  )

  val participantsPanel = new ListBox[String]()
  participantsPanel.setDataProvider(participantsDataProvider)

  val startButton = new Button("Start", _ => RaffleServer.raffleServer ! StartRaffle)
  startButton.setVisible(false)
  startButton.setEnabled(false)

  val removeButton = new Button("Remove", _ => RaffleServer.raffleServer ! Remove(participantsPanel.getValue))
  removeButton.setVisible(false)
  removeButton.setEnabled(false)

  val removeAllButton = new Button("Remove All", _ => RaffleServer.raffleServer ! RemoveAll)
  removeAllButton.setVisible(false)
  removeAllButton.setEnabled(false)

  val winnerCaption = "Winner:"
  val noWinnerCaption: String = winnerCaption + " -"

  val winnerLabel = new H2(noWinnerCaption)

  val footer = new Anchor("https://github.com/scala-vienna/vaadin-raffle", "Source code (GitHub)")

  add(
    new H1(title),
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
    session ! VaactorSession.RequestSessionState
    RaffleServer.raffleServer ! GetParticipants
    RaffleServer.raffleServer ! GetWinner
  }

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: Receive = {
    case SessionState.Participating(name) =>
      enterButton.setVisible(false)
      leaveButton.setVisible(true)
      participantName.setVisible(false)
      participantNameLabel.setText(s"Good luck, $name!")
      participantNameLabel.setVisible(true)

    case SessionState.Coordinator =>
      enterButton.setVisible(false)
      leaveButton.setVisible(true)
      participantName.setVisible(false)
      participantNameLabel.setText(s"You are the coordinator!")
      participantNameLabel.setVisible(true)
      startButton.setVisible(true)
      removeButton.setVisible(true)
      removeAllButton.setVisible(true)

    case SessionState.None =>
      enterButton.setVisible(true)
      leaveButton.setVisible(false)
      participantName.setVisible(true)
      participantNameLabel.setVisible(false)
      startButton.setVisible(false)
      removeButton.setVisible(false)
      removeAllButton.setVisible(false)

    case Winner(name) =>
      if (name.isDefined) {
        winnerLabel.setText(s"$winnerCaption ${name.get}")
      }
      else {
        winnerLabel.setText(noWinnerCaption)
      }

    // User entered raffle, update participants list
    case Participants(participants) =>
      participantsList.clear()
      participantsList.addAll(participants.asJava)
      participantsDataProvider.refreshAll()

      startButton.setEnabled(participants.nonEmpty)
      removeButton.setEnabled(participants.nonEmpty)
      removeAllButton.setEnabled(participants.nonEmpty)

    case Error(error) =>
      Notification.show(error)
  }
}
