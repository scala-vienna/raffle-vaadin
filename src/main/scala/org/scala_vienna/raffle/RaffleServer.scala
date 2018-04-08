package org.scala_vienna.raffle

import org.vaadin.addons.vaactor.VaactorServlet
import akka.actor.{Actor, ActorRef, Props}
import scala.collection.immutable.HashSet
import scala.util.Random

object RaffleServer {

  /** Participant wants to enter raffle, processed by server
    *
    * @param name name of new participant
    */
  case class Participate(name: String, session: ActorRef)

  case class Coordinate(session: ActorRef)

  case class Leave(session: ActorRef)

  case object StartRaffle

  case class Remove(name: String)

  case object RemoveAll

  case object RegisterSession

  case class ParticipateSuccess(name: String)

  case object CoordinateSuccess

  case object LeaveSuccess

  case class Winner(name: Option[String])

  case class Participants(participants: List[String])

  case class Error(error: String)

  case class GetParticipants(ui: ActorRef)

  case class GetWinner(ui: ActorRef)

  /** ActoRef of raffle actor */
  val raffleServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "raffleServer")

  /** Actor handling chatroom */
  class ServerActor extends Actor {

    // List of sessions
    private var sessions = Map.empty[ActorRef, SessionState.State]

    // List of participants
    private var participants = List.empty[String]

    private var winner: Option[String] = None

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
          BroadcastParticipants()
        }
      case Coordinate(session) => {
        updateState(session, SessionState.Coordinator)
      }
      case Leave(session) =>
        sessions.get(session) match {
          case Some(SessionState.None) =>
            sender ! Error("Cannot leave raffle. Not participating.")
          case Some(SessionState.Participating(name)) =>
            participants = participants.filter(_ != name)
            updateState(session, SessionState.None)
            BroadcastParticipants()
          case Some(SessionState.Coordinator) =>
            updateState(session, SessionState.None)
          case None =>
            sender ! Error("Cannot leave raffle. Not participating.")
        }
      case StartRaffle =>
        if (participants.size > 0) {
          val winnerIndex = Random.nextInt(participants.size)
          winner = Some(participants(winnerIndex))
          broadcast(Winner(winner))
        }
      case Remove(name) =>
        val matchingSessions = sessions.filter {
          case (_, SessionState.Participating(`name`)) => true
          case _ => false
        }.map(_._1)
        for (session <- matchingSessions) {
          updateState(session, SessionState.None)
        }
        participants = participants.filter(_ != name)
        BroadcastParticipants()
      case RemoveAll =>
        val allSessions: Iterable[ActorRef] = sessions.keys
        for (session <- allSessions) {
          if (sessions(session).isInstanceOf[SessionState.Participating]) {
            updateState(session, SessionState.None)
          }
        }
        participants = List.empty[String]
        BroadcastParticipants()
      // Session wants to register (for listening to messages broadcasted by the server)
      case RegisterSession =>
        sessions += (sender -> SessionState.None)
        sender ! Participants(participants)
        sender ! Winner(winner)

      case GetParticipants(ui) => ui ! Participants(participants)

      case GetWinner(ui) => ui ! Winner(winner)
    }

    private def updateState(session: ActorRef, newState: SessionState.State): Unit = {
      sessions = sessions.updated(session, newState)
      session ! newState
    }

    private def BroadcastParticipants() = broadcast(Participants(participants))

    /** Send message to every session
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = sessions.keys foreach { _ ! msg }

  }

}
