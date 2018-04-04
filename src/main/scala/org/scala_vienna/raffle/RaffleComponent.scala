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
  /** Contains list of chatroom menbers */
  val participantsList = new java.util.ArrayList[String]()
  val participantsDataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](participantsList)
  val participantsPanel: ListSelect[String] = new ListSelect("Raffle Participants", participantsDataProvider) {
    setWidth(100, Sizeable.Unit.PIXELS)
  }

  /** Contains username */
  val userName = new TextField()

  val loginPanel: HorizontalLayout = new HorizontalLayout {
    setSpacing(true)
    addComponents(
      userName,
      new Button("Enter Raffle", _ => { RaffleServer.raffleServer ! Subscribe(Client(userName.getValue, self)) })
    )
  }


  /** Contains user interface for login/logout and sending of messages */
  val userPanel = new Panel(
    new VerticalLayout {
      setSpacing(true)
      setMargin(true)
      addComponents(
        new HorizontalLayout {
          setSpacing(true)
          addComponents(loginPanel)
        })
    }
  )

  val startButton = new Button("Start", _ => { RaffleServer.raffleServer ! StartRaffle })

  val winnerLabel: Label = new Label {
    setValue("Winner: ")
    addStyleName(ValoTheme.LABEL_H2)
  }

  startButton.setVisible(false)

  setCompositionRoot(new VerticalLayout {

    addComponents(
      new Label {
        setValue(title)
        addStyleName(ValoTheme.LABEL_H1)
      },
      new HorizontalLayout {
        setSpacing(true)
        addComponents(
          new VerticalLayout {
            setSpacing(true)
            addComponents(userPanel)
          },
          participantsPanel)
      },
      winnerLabel,
      startButton)
  })


  /** Receive function, is called in context of VaadinUI (via ui.access) */
  override def receive: PartialFunction[Any, Unit] = {
    // User entered chatroom, update member list
    case Enter(participants) =>

      participantsList.clear()
      participantsList.addAll(participants.asJava)
      participantsDataProvider.refreshAll()

    case SubscriptionFailure(error) =>
      Notification.show(error, Notification.Type.WARNING_MESSAGE)

    case YouAreCoordinator => startButton.setVisible(true)

    case Result(name) => {
      winnerLabel.setValue(s"Winner: $name")
    }
  }
}
