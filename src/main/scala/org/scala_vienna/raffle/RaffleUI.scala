package org.scala_vienna.raffle

import com.vaadin.flow.component.orderedlayout.VerticalLayout
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.Route
import com.vaadin.flow.shared.communication.PushMode
import com.vaadin.flow.shared.ui.Transport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@Push(
  value = PushMode.AUTOMATIC,
  transport = Transport.WEBSOCKET
)
@Route("dummy")
@Theme(
  value = classOf[Lumo],
  variant = Lumo.DARK
)
class RaffleUI extends VerticalLayout {

  add(new RaffleComponent("Vaactor Raffle"))

}
