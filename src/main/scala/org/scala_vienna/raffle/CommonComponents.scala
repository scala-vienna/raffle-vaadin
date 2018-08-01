package org.scala_vienna.raffle

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.{Anchor, H1, Span}
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import org.vaadin.addons.vaactor.loadedConfig

object CommonComponents {

  def hesder(text: String): Component = new H1(text) {

    getElement.setAttribute("style",
      "margin-top: 0.50em; margin-bottom: 0.25em; text-shadow: -5px -5px 10px white;")

  }

  def footer(): Component = new HorizontalLayout {

    getElement.setAttribute("style",
      "position: fixed; bottom: 0px; width: 100%; margin: 0px; font-size: small;")

    add(
      new Anchor(loadedConfig.getString("raffle.powered-by"), "Powered by Vaactor"),
      new Span("-"),
      new Anchor(loadedConfig.getString("raffle.source-code"), "Source code (GitHub)")
    )

  }

}
