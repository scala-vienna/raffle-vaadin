package org.scala_vienna.raffle

import akka.actor.Props
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
class RaffleServlet extends VaactorServlet {
  /** Define session actor to be created for every session */
  override val sessionProps = Some(Props(classOf[Session]))
}
