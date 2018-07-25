package org.scala_vienna.raffle

import akka.actor.Props
import com.vaadin.flow.server.VaadinServletConfiguration
import javax.servlet.annotation.WebServlet
import org.vaadin.addons.vaactor.VaactorSessionServlet

/** Define servlet, url pattern and ui-class to start
  *
  */
@WebServlet(
  urlPatterns = Array("/*"),
  asyncSupported = true
)
@VaadinServletConfiguration(
  productionMode = false
)
class RaffleServlet extends VaactorSessionServlet {
  /** Define session actor to be created for every session */
  override val sessionProps = Props(classOf[Session])
}
