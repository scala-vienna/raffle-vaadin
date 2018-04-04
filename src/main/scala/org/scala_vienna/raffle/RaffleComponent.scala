package org.scala_vienna.raffle

import org.scala_vienna.raffle.RaffleServer._
import org.vaadin.addons.vaactor.Vaactor.VaactorComponent
import org.vaadin.addons.vaactor.VaactorUI
import com.vaadin.data.provider.{ DataProvider, ListDataProvider }
import com.vaadin.server.Sizeable
import com.vaadin.ui.themes.ValoTheme
import com.vaadin.ui._
import scala.collection.JavaConverters._

class RaffleComponent(override val vaactorUI: VaactorUI, title: String) extends CustomComponent with VaactorComponent {
  /** Contains list of raffle participants */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)
  val participantWidth = 200

  /** Contains participant name */
  val participantName = new TextField("Name:")
  participantName.setWidth(participantWidth, Sizeable.Unit.PIXELS)

  val enterButton = new Button("Enter Raffle", _ => {
    RaffleServer.raffleServer ! Participate(participantName.getValue)
  })

  val enterPanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    addComponents(
      participantName,
      enterButton)
    setComponentAlignment(participantName, Alignment.BOTTOM_LEFT)
    setComponentAlignment(enterButton, Alignment.BOTTOM_LEFT)
  }

  val participantsPanel: ListSelect[String] = new ListSelect("Participants:", participantsDataProvider) {
    setWidth(participantWidth, Sizeable.Unit.PIXELS)
  }

  val startButton = new Button("Start", _ => { RaffleServer.raffleServer ! StartRaffle })
  startButton.setVisible(false)

  val clearButton = new Button("Clear", _ => { RaffleServer.raffleServer ! Clear })
  clearButton.setVisible(false)

  val winnerCaption = "Winner:"

  val winnerLabel: Label = new Label {
    setValue(winnerCaption)
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
      clearButton,
      winnerLabel)
  })

  RaffleServer.raffleServer ! RegisterClient

  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: PartialFunction[Any, Unit] = {
    // User entered raffle, update participants list
    case Participants(participants) =>

      participantsList.clear()
      participantsList.addAll(participants.asJava)
      participantsDataProvider.refreshAll()

    case ParticipateFailure(error) =>
      Notification.show(error, Notification.Type.WARNING_MESSAGE)

    case YouAreCoordinator => {
      startButton.setVisible(true)
      clearButton.setVisible(true)
    }

    case Winner(name) => {
      winnerLabel.setValue(s"$winnerCaption $name")
    }
  }
}
