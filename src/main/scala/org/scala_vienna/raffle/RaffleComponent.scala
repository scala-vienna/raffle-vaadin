package org.scala_vienna.raffle

import java.util
import akka.actor.ActorRef
import org.scala_vienna.raffle.RaffleServer.{LeaveSuccess, _}
import org.vaadin.addons.vaactor.Vaactor.VaactorComponent
import org.vaadin.addons.vaactor.VaactorUI
import com.vaadin.data.provider.{DataProvider, ListDataProvider}
import com.vaadin.server.Sizeable
import com.vaadin.shared.ui.ContentMode
import com.vaadin.ui.themes.ValoTheme
import com.vaadin.ui._
import org.scala_vienna.raffle.Session.RegisterUI
import scala.collection.JavaConverters._


class RaffleComponent(override val vaactorUI: VaactorUI, title: String, sessionActor: ActorRef) extends CustomComponent with VaactorComponent {
  /** This component's name if participating */
  var myName: Option[String] = None

  /** Contains list of raffle participants */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)
  val participantWidth = 200

  /** Contains participant name */
  val participantName = new TextField("Name:")
  participantName.setWidth(participantWidth, Sizeable.Unit.PIXELS)

  val participantNameLabel = new Label("", ContentMode.HTML)
  participantNameLabel.setWidth(participantWidth, Sizeable.Unit.PIXELS)
  participantNameLabel.setVisible(false)

  val enterButton = new Button("Enter Raffle", _ => {
    if (participantName.getValue == "raffle2018coord") {
      RaffleServer.raffleServer ! Coordinate(sessionActor)
    }
    else {
      RaffleServer.raffleServer ! Participate(participantName.getValue, sessionActor)
    }
  })

  val leaveButton = new Button("Leave Raffle", _ => { RaffleServer.raffleServer ! Leave(sessionActor) })
  leaveButton.setVisible(false)

  val enterPanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    addComponents(
      participantName,
      participantNameLabel,
      enterButton,
      leaveButton)
    setComponentAlignment(participantName, Alignment.BOTTOM_LEFT)
    setComponentAlignment(participantNameLabel, Alignment.BOTTOM_LEFT)
    setComponentAlignment(enterButton, Alignment.BOTTOM_LEFT)
    setComponentAlignment(leaveButton, Alignment.BOTTOM_LEFT)
  }

  val participantsPanel: ListSelect[String] = new ListSelect("Participants:", participantsDataProvider) {
    setWidth(participantWidth, Sizeable.Unit.PIXELS)
  }

  val startButton = new Button("Start", _ => { RaffleServer.raffleServer ! StartRaffle })
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

  val removeAllButton = new Button("Remove All", _ => { RaffleServer.raffleServer ! RemoveAll })
  removeAllButton.setVisible(false)
  removeAllButton.setEnabled(false)

  val winnerCaption = "Winner:"
  val noWinnerCaption = winnerCaption + " -"

  val winnerLabel: Label = new Label {
    setValue(noWinnerCaption)
    addStyleName(ValoTheme.LABEL_H2)
  }

  setCompositionRoot(new VerticalLayout {
    addComponents(
      new Label {
        setValue(title)
        addStyleName(ValoTheme.LABEL_H1)
      },
      enterPanel,
      participantsPanel,
      startButton,
      removeButton,
      removeAllButton,
      winnerLabel)
  })

  sessionActor ! RegisterUI

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: PartialFunction[Any, Unit] = {
    case SessionState.Participating(name) =>
      enterButton.setVisible(false)
      leaveButton.setVisible(true)
      participantName.setVisible(false)
      participantNameLabel.setValue(s"Good luck, <b>$name</b>!")
      participantNameLabel.setVisible(true)

    case SessionState.Coordinator =>
      enterButton.setVisible(false)
      leaveButton.setVisible(true)
      participantName.setVisible(false)
      participantNameLabel.setValue(s"You are a coordinator!")
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
        winnerLabel.setValue(s"$winnerCaption ${name.get}")
      }
      else {
        winnerLabel.setValue(noWinnerCaption)
      }

    // User entered raffle, update participants list
    case Participants(participants) =>
      participantsList.clear()
      participantsList.addAll(participants.asJava)
      participantsDataProvider.refreshAll()

      startButton.setEnabled(participants.size > 0)
      removeButton.setEnabled(participants.size > 0)
      removeAllButton.setEnabled(participants.size > 0)

    case Error(error) =>
      Notification.show(error, Notification.Type.WARNING_MESSAGE)
  }
}
