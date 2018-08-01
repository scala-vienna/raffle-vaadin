package org.scala_vienna.raffle

import akka.actor.Actor.Receive
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.{HorizontalLayout, VerticalLayout}
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.component.{AttachEvent, DetachEvent}
import com.vaadin.flow.router.{BeforeEvent, HasUrlParameter, Route}
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import org.vaadin.addons.vaactor.{Vaactor, loadedConfig}

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

  override def setParameter(event: BeforeEvent, parameter: String): Unit = raffleKey.value = parameter

  override def onAttach(attachEvent: AttachEvent): Unit = {
    super.onAttach(attachEvent)
    Manager ! Manager.LookupKey(raffleKey.value) // must not receive anything before attach
  }

  override def onDetach(detachEvent: DetachEvent): Unit = {
    Manager ! Manager.Close(raffle.value.id) // close raffle on detach - avoids memory leak on detach by timeout
    super.onDetach(detachEvent)
  }

  private def qrCode(id: String): GraniteQRCodeGenerator = {
    val url = loadedConfig.getString("raffle.external-url")
    val path = ui.getRouter.getUrl(classOf[ParticipantView], id)
    new GraniteQRCodeGenerator(url + path)
      .withMode(GraniteQRCodeGenerator.Octet)
      .withEcclevel(GraniteQRCodeGenerator.High)
  }

  private def processManager(reply: Manager.Reply): Unit = reply match {
    case r: Manager.Raffle =>
      raffle.value = r
      add(
        new H1(s"Vaactor Raffle ${raffle.value.id}"),
        new AdminComponent(raffle.value),
        new HorizontalLayout(
          new Button("QR Code", _ =>
            ConfirmDialog(qrCode(raffle.value.id))
              .withButton("Magnify", _ =>
                ConfirmDialog(qrCode(raffle.value.id).withModulesize(12))
                  .onCancel(_ => {})
                  .open()
              )
              .onCancel(_ => {})
              .open()
          ), new Button("Close raffle", _ =>
            ConfirmDialog("This will terminate the raffle! - Continue?")
              .onOK(_ => Manager ! Manager.Close(raffle.value.id))
              .onCancel(_ => {})
              .open()
          )
        )
      )
    case Manager.Error(msg) =>
      Notification.show(msg)
      ui.navigate("")
    case Manager.Closed(id) =>
      Notification.show(s"raffle $id closed")
  }

  override def receive: Receive = {
    case reply: Manager.Reply => processManager(reply)
  }

}
