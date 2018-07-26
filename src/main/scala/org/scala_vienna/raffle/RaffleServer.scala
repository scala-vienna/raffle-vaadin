package org.scala_vienna.raffle

import akka.actor.Actor
import org.vaadin.addons.vaactor.VaactorSession

import scala.util.Random

object RaffleServer {

  /** Participant wants to enter raffle, processed by server
    *
    * @param name name of new participant
    */
  case class Participate(name: String)

  /** Client wants to stop participating or being coordinator
    *
    * @param name name of leaving participant
    */
  case class Leave(name: String)

  /** Client wants to start the Raffle, processed by server */
  case object StartRaffle

  /** Coordinator wants to remove one participant
    *
    * @param name name of participant, processed by server
    */
  case class Remove(name: String)

  /** Coordinator wants to remove all participants, processed by server */
  case object RemoveAll

  /** The winner is... or no winner yet, sent by server or session */
  case class Winner(name: Option[String])

  /** The current participants are..., sent by server or session */
  case class Participants(participants: List[String])

  case class Error(error: String)

  /** Someone wants to know the current list of particpants */
  case object GetParticipants

  /** Someone wants to know the current winner */
  case object GetWinner

  /** Actor handling Raffle server */
  class ServerActor extends Actor with VaactorSession[Int] {

    override val initialSessionState: Int = 0

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
    override val sessionBehaviour: Receive = {
      // Session wants to participate
      case Participate(name) =>
        // no name, reply with failure
        if (name.isEmpty)
          sender ! Error("Empty name not valid")
        // duplicate name, reply with failure
        else if (participants.contains(name))
          sender ! Error(s"Name '$name' already participating")
        // add session to raffle, broadcast new participant list to sessions
        else {
          participants :+= name
          // todo - updateState(session, SessionState.Participating(name))
        }
      case Leave(name) =>
        participants = participants.filter(_ != name)
      case StartRaffle =>
        if (participants.nonEmpty) {
          val winnerIndex = Random.nextInt(participants.size)
          winner = Some(participants(winnerIndex))
        }
      case Remove(name) =>
        participants = participants.filter(_ != name)
      // todo - updateState(session, SessionState.Listening)
      case RemoveAll =>
        participants = List.empty[String]
      // todo - for all participants updateState(session, SessionState.Listening)

      case GetParticipants => sender ! Participants(participants)

      case GetWinner => sender ! Winner(winner)
    }

  }

}
