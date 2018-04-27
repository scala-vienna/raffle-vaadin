package org.scala_vienna.raffle

import akka.actor.{Actor, ActorRef, Props}
import org.vaadin.addons.vaactor.VaactorServlet

import scala.util.Random

object RaffleServer {

  /** Participant wants to enter raffle, processed by server
    *
    * @param name name of new participant
    */
  case class Participate(name: String, session: ActorRef)

  /** Client wants to be coordinator (can start, Reset, ...)
    *
    * @param session session ActorRef of client, processed by server
    */
  case class Coordinate(session: ActorRef)

  /** Client wants to stop participating or being coordinator
    *
    * @param session session ActorRef of client, processed by server
    */
  case class Leave(session: ActorRef)

  /** Client wants to start the Raffle, processed by server */
  case object StartRaffle

  /** Coordinator wants to remove one participant
    *
    * @param name name of participant, processed by server
    */
  case class Remove(name: String)

  /** Coordinator wants to remove all participants, processed by server */
  case object RemoveAll

  case object RegisterSession

  /** The winner is... or no winner yet, sent by server or session */
  case class Winner(name: Option[String])

  /** The current participants are..., sent by server or session */
  case class Participants(participants: List[String])

  case class Error(error: String)

  /** Someone wants to know the current list of particpants
    *
    * @param ui UI ActorRef to send the info to, processed by server
    */
  case class GetParticipants(ui: ActorRef)

  /** Someone wants to know the current winner
    *
    * @param ui UI ActorRef to send the info to, processed by server
    */
  case class GetWinner(ui: ActorRef)

  /** ActoRef of raffle actor */
  val raffleServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "raffleServer")

  /** Actor handling Raffle server */
  class ServerActor extends Actor {

    //noinspection ActorMutableStateInspection
    // Current sessions with their state
    private var sessions = Map.empty[ActorRef, SessionState.State]

    //noinspection ActorMutableStateInspection
    // Current participants
    private var _participants = List.empty[String]

    private def participants: List[String] = _participants

    private def participants_=(newVal: List[String]): Unit = {
      if (_participants.size != newVal.size) {
        winner = None
      }
      _participants = newVal
      broadcast(Participants(_participants))
    }

    //noinspection ActorMutableStateInspection
    // Current winner
    private var _winner: Option[String] = None

    private def winner: Option[String] = _winner

    private def winner_=(newVal: Option[String]): Unit = {
      _winner = newVal
      broadcast(Winner(_winner))
    }

    /** Process received messages */
    def receive: Receive = {
      // Session wants to participate
      case Participate(name, session) =>
        // no name, reply with failure
        if (name.isEmpty)
          sender ! Error("Empty name not valid")
        // duplicate name, reply with failure
        else if (participants.contains(name))
          sender ! Error(s"Name '$name' already participating")
        // add session to raffle, broadcast new participant list to sessions
        else {
          participants :+= name
          updateState(session, SessionState.Participating(name))
        }
      case Coordinate(session) =>
        if (sessions.values.toSeq.contains(SessionState.Coordinator)) {
          sender ! Error(s"A coordinator is already active.")
        } else {
          sessions.get(session) match {
            case Some(SessionState.None) =>
              updateState(session, SessionState.Coordinator)
            case _ =>
              sender ! Error("Session cannot be coordinator because it is unkown/participating/coordinator.")
          }
        }
      case Leave(session) =>
        sessions.get(session) match {
          case Some(SessionState.Participating(name)) =>
            participants = participants.filter(_ != name)
            updateState(session, SessionState.None)
          case Some(SessionState.Coordinator) =>
            updateState(session, SessionState.None)
          case Some(SessionState.None) | None =>
            sender ! Error("Cannot leave raffle. Not participating.")
        }
      case StartRaffle =>
        if (participants.nonEmpty) {
          val winnerIndex = Random.nextInt(participants.size)
          winner = Some(participants(winnerIndex))
        }
      case Remove(name) =>
        val matchingSessions = sessions.collect {
          case (session, SessionState.Participating(`name`)) => session
        }
        for (session <- matchingSessions) {
          updateState(session, SessionState.None)
        }
        participants = participants.filter(_ != name)
      case RemoveAll =>
        val allSessions: Iterable[ActorRef] = sessions.keys
        for (session <- allSessions) {
          if (sessions(session).isInstanceOf[SessionState.Participating]) {
            updateState(session, SessionState.None)
          }
        }

        participants = List.empty[String]
      // Session wants to register (for listening to messages broadcasted by the server)
      case RegisterSession =>
        if (!sessions.contains(sender)) {
          sessions += (sender -> SessionState.None)
        }
        sender ! Participants(participants)
        sender ! Winner(winner)

      case GetParticipants(ui) => ui ! Participants(participants)

      case GetWinner(ui) => ui ! Winner(winner)
    }

    private def updateState(session: ActorRef, newState: SessionState.State): Unit = {
      sessions = sessions.updated(session, newState)
      session ! newState
    }

    /** Send message to every session
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = sessions.keys foreach {
      _ ! msg
    }

  }

}
