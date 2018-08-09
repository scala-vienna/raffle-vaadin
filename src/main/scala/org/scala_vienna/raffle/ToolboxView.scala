package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.{BeforeEvent, HasUrlParameter, Route}
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.vaadin.addons.vaactor.{Vaactor, VaactorSession}

import scala.util.Random

/** Toolbox View for administrator of a raffle
  *
  * Activates component with tool buttons with the raffle reference.
  *
  * To activate copy the Url of AdminView to a new browser tab and change "admin" to "toolbox".
  */
@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
@Route("toolbox")
@Theme(
  value = classOf[Lumo],
  variant = Lumo.DARK
)
class ToolboxView extends VerticalLayout
  with HasUrlParameter[String] with Vaactor.HasActor {

  val raffleKey: DelayedValue[String] = DelayedValue[String]
  val raffle: DelayedValue[Manager.Raffle] = DelayedValue[Manager.Raffle]

  override def setParameter(event: BeforeEvent, parameter: String): Unit = raffleKey.value = parameter

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    Manager ! Manager.LookupKey(raffleKey.value) // must not receive anything before attach
  }

  override def receive: Receive = {
    case r: Manager.Raffle =>
      raffle.value = r
      raffle.value ! VaactorSession.Subscribe
      add(
        CommonComponents.header(s"Toolbox Raffle ${raffle.value.id}"),
        new Button("Add Participant", _ => raffle.value ! RaffleServer.Enter(Random.alphanumeric.take(10).mkString)),
        CommonComponents.footer()
      )
    case RaffleServer.Terminated =>
      ConfirmDialog("Raffle terminated!").onOK(_ => ui.navigate("")).open()
    case _ => // avoid dead letter logs
  }

}
