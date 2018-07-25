package org.scala_vienna.raffle

import com.vaadin.flow.component.html.H1
import com.vaadin.flow.component.orderedlayout.VerticalLayout

/** View for administrator of a raffle */
class AdminView(raffle: Manager.Raffle) extends VerticalLayout {

  add(new H1(s"Vaactor Raffle (${raffle.id})"))

}
