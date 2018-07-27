package org.scala_vienna.raffle

import com.vaadin.flow.component.html.H2
import org.scala_vienna.raffle.WinnerPanel._

object WinnerPanel {

  val winnerCaption = "Winner:"
  val noWinnerCaption: String = winnerCaption + " -"

}

class WinnerPanel extends H2(noWinnerCaption) {

  def show(winner: Option[String]): Unit = {
    if (winner.isDefined)
      setText(s"$winnerCaption ${winner.get}")
    else
      setText(noWinnerCaption)
  }

}
