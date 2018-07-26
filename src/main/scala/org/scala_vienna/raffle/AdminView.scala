package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
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

/** View for administrator of a raffle
  *
  * Activates a AdminComponent with the raffle reference.
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
@Route("admin")
@Theme(
  value = classOf[Lumo],
  variant = Lumo.DARK
)
class AdminView extends VerticalLayout
  with HasUrlParameter[String] with Vaactor.HasActor {

  val raffleKey: DelayedValue[String] = DelayedValue[String]
  val raffle: DelayedValue[Manager.Raffle] = DelayedValue[Manager.Raffle]

  val title = new H1()

  add(title)

  override def setParameter(event: BeforeEvent, parameter: String): Unit = raffleKey.value = parameter

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    Manager ! Manager.LookupKey(raffleKey.value) // must no receive anything before attach
  }

  private def processManager(reply: Manager.Reply): Unit = reply match {
    case r: Manager.Raffle =>
      raffle.value = r
      title.setText(s"Vaactor Raffle ${raffle.value.id}")
      add(
        new AdminComponent(raffle.value),
        new Button("Close raffle", { _ => Manager ! Manager.Close(raffle.value.id) })
      )
    case Manager.Error(msg) =>
      Notification.show(msg)
      ui.navigate("")
    case Manager.Closed(id) =>
      Notification.show(s"raffle $id closed")
      ui.navigate("")
  }

  override def receive: Receive = {
    case reply: Manager.Reply => processManager(reply)
  }

}
