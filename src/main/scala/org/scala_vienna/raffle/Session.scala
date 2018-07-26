package org.scala_vienna.raffle

import akka.actor.Actor
import org.vaadin.addons.vaactor.VaactorSession

object Session {

  sealed trait Command

  /** sends VaactorSession.RequestState to Raffle */
  case object RequestRaffleState extends Command

  /** replies Entered od Left to sender, depending on state */
  case object ReportState extends Command

  /** Session state, holds name of participant  */
  case class State(name: Option[String]) {

    def withName(name: String): State = copy(Some(name))

    def withoutName(): State = copy(None)

  }

}

/** Session state consists of participant name.
  *
  * Sends all received Raffle commands to raffle.
  * Broadcasts all received Raffle replies to own subscribers.
  *
  * State is maintained based on Replies from raffle.
  * Raffle instance is sent by ParticipantView.
  *
  */
class Session extends Actor with VaactorSession[Session.State] {

  private var raffle: Option[Manager.Raffle] = None

  override val initialSessionState: Session.State = Session.State(None)

  def send2Raffle(msg: Any): Unit = for (r <- raffle) r ! msg

  override val sessionBehaviour: Receive = {
    case command: RaffleServer.Command => command match {
      case leave: RaffleServer.Leave =>
        if (sessionState.name.isDefined) send2Raffle(leave.copy(name = sessionState.name.get))
      case _ =>
        send2Raffle(command)
    }
    case reply: RaffleServer.Reply => reply match {
      case entered: RaffleServer.Entered =>
        sessionState = sessionState.withName(entered.name)
        broadcast(entered)
      case left: RaffleServer.Left =>
        sessionState = sessionState.withoutName()
        broadcast(left)
      case _ =>
        broadcast(reply)
    }
    case command: Session.Command => command match {
      case Session.RequestRaffleState =>
        send2Raffle(VaactorSession.RequestSessionState)
      case Session.ReportState => sessionState.name match {
        case Some(name) => sender ! RaffleServer.Entered(name)
        case None => sender ! RaffleServer.Left("")
      }
    }
    case r: Manager.Raffle =>
      raffle = Some(r)
      send2Raffle(VaactorSession.Subscribe)
  }

  override def postStop(): Unit = send2Raffle(VaactorSession.Unsubscribe)

}
