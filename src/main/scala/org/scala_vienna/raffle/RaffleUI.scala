package org.scala_vienna.raffle

import org.vaadin.addons.vaactor.VaactorUI
import com.vaadin.annotations.Push
import com.vaadin.server.VaadinRequest
import com.vaadin.shared.communication.PushMode
import com.vaadin.shared.ui.ui.Transport

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
class RaffleUI extends VaactorUI {

  override def init(request: VaadinRequest): Unit =
    setContent(new RaffleComponent(this, "Vaactor Raffle"))

}
