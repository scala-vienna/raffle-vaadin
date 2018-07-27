package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.{BeforeEvent, HasUrlParameter, Route}
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.vaadin.addons.vaactor.Vaactor

/** View for participant of a raffle.
  *
  * Initializes the session actor with the raffle reference and activates a ParticipantComponent.
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
@Route("")
@Theme(
  value = classOf[Lumo],
  variant = Lumo.DARK
)
class ParticipantView extends VerticalLayout
  with HasUrlParameter[String] with Vaactor.HasActor with Vaactor.HasSession {

  val raffleId: DelayedValue[String] = DelayedValue[String]
  val raffle: DelayedValue[Manager.Raffle] = DelayedValue[Manager.Raffle]

  val title = new H1()

  add(
    title
  )

  override def setParameter(event: BeforeEvent, parameter: String): Unit = raffleId.value = parameter

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    Manager ! Manager.Lookup(raffleId.value) // must not receive anything before attach
  }

  override def receive: Receive = {
    case reply: Manager.Reply => reply match {
      case r: Manager.Raffle =>
        raffle.value = r
        title.setText(s"Vaactor Raffle ${raffle.value.id}")
        session ! raffle.value // tell session the raffle to subscribe
        add(new ParticipantComponent())
      case Manager.Error(msg) =>
        Notification.show(msg)
        ui.navigate("")
      case Manager.Closed(id) =>
        Notification.show(s"raffle $id closed")
        ui.navigate("")
    }
  }

}
