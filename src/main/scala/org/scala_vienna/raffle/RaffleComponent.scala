package org.scala_vienna.raffle

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.html.{Anchor, H1, H2, Label}
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.textfield.TextField
import com.vaadin.flow.data.provider.{DataProvider, ListDataProvider}
import org.scala_vienna.raffle.RaffleServer._
import org.vaadin.addons.vaactor.Vaactor

import scala.collection.JavaConverters._


class RaffleComponent(title: String)
  extends VerticalLayout with Vaactor.HasActor with Vaactor.SubscribeSession {

  /** This component's name if participating */
  var myName: Option[String] = None

  /** Contains list of raffle participants */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)
  val participantWidth = "200px"

  /** Contains participant name */
  val participantName = new TextField("Name:")
  participantName.setWidth(participantWidth)

  /* todo - Shortcut Handling?
  val enterListener: ShortcutListener = new ShortcutListener("Submit", ShortcutAction.KeyCode.ENTER, Array.empty[Int]: _*) {
    override def handleAction(sender: scala.Any, target: scala.Any): Unit = {
      enterButton.click()
    }
  }

  private var registration: Option[Registration] = None

  participantName.addBlurListener((_: FieldEvents.BlurEvent) => {
    for (r <- registration) r.remove()
    registration = None
  })

  participantName.addFocusListener((_: FieldEvents.FocusEvent) => {
    if (registration.isEmpty)
      registration = Some(participantName.addShortcutListener(enterListener))
  })
  */

  participantName.focus()

  val participantNameLabel = new Label("")
  participantNameLabel.setWidth(participantWidth)
  participantNameLabel.setVisible(false)

  val enterButton = new Button("Enter Raffle", _ => {
    if (participantName.getValue == "raffle2018coord") {
      RaffleServer.raffleServer ! Coordinate(session)
    }
    else {
      RaffleServer.raffleServer ! Participate(participantName.getValue, session)
    }
  })

  val leaveButton = new Button("Leave Raffle", _ => {
    RaffleServer.raffleServer ! Leave(session)
  })
  leaveButton.setVisible(false)

  val enterPanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    add(
      participantName,
      participantNameLabel,
      enterButton,
      leaveButton)
    /* todo - FlexLayout ?
    setComponentAlignment(participantName, Alignment.BOTTOM_LEFT)
    setComponentAlignment(participantNameLabel, Alignment.BOTTOM_LEFT)
    setComponentAlignment(enterButton, Alignment.BOTTOM_LEFT)
    setComponentAlignment(leaveButton, Alignment.BOTTOM_LEFT)
    */
  }

  val participantsPanel: Grid[String] = new Grid[String]() {
    setWidth(participantWidth)
    addColumn(s => s).setHeader("Participants:")
    setDataProvider(participantsDataProvider)
  }

  val startButton = new Button("Start", _ => {
    RaffleServer.raffleServer ! StartRaffle
  })
  startButton.setVisible(false)
  startButton.setEnabled(false)

  val removeButton = new Button("Remove", _ => {
    val javaArray: Array[String] = participantsPanel.getSelectedItems.toArray(new Array[String](0))

    for (selected <- javaArray) {
      RaffleServer.raffleServer ! Remove(selected)
    }
  })
  removeButton.setVisible(false)
  removeButton.setEnabled(false)

  val removeAllButton = new Button("Remove All", _ => {
    RaffleServer.raffleServer ! RemoveAll
  })
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
    footer)

  // todo - wait for attach!!! - session ! VaactorSession.RequestSessionState
  RaffleServer.raffleServer ! GetParticipants
  RaffleServer.raffleServer ! GetWinner

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: PartialFunction[Any, Unit] = {
    case SessionState.Participating(name) =>
      enterButton.setVisible(false)
      leaveButton.setVisible(true)
      participantName.setVisible(false)
      participantNameLabel.setText(s"Good luck, <b>$name</b>!")
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
