package org.scala_vienna.raffle

import javax.servlet.annotation.WebServlet

import org.vaadin.addons.vaactor.VaactorServlet
import com.vaadin.annotations.VaadinServletConfiguration

/** Define servlet, url pattern and ui-class to start
  *
  */
@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false,
  ui = classOf[RaffleUI]
)
class RaffleServlet extends VaactorServlet
