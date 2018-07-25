package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.vaadin.addons.vaactor.Vaactor

/** View for creator of a raffle */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
@Route("")
@Theme(
  value = classOf[Lumo],
  variant = Lumo.DARK
)
class CreatorView extends VerticalLayout with Vaactor.HasActor {

  add(
    new H1("Vaactor Raffle"),
    new Button("Create raffle", { _ => Manager ! Manager.Create })
  )

  override def receive: Receive = {
    case reply: Manager.Reply => reply match {
      case raffle: Manager.Raffle =>
        ui.navigate(s"admin/${raffle.id}")
      case Manager.Error(msg) =>
        Notification.show(msg)
      case Manager.Closed(_) =>
    }
  }

}
