package org.scala_vienna.raffle

import akka.actor.{Actor, ActorRef}
import org.vaadin.addons.vaactor.VaactorSession

import scala.util.Random

/** Contains all Messages handled by RaffleActor.
  *
  * Participants consist of a name and an actor - nothing else!
  * The RaffleActor does not need to know about the behavior of this actor.
  *
  * The current implementation of ParticipantView uses the session actor for the participant.
  *
  */
object RaffleServer {

  /** commands for RaffleActor, processed by server */
  sealed trait Command

  /** Participant wants to enter raffle */
  case class Enter(name: String) extends Command

  /** Participant wants to leave the raffle */
  case class Leave(name: String) extends Command

  /** Start the Raffle and select winner */
  case object SelectWinner extends Command

  /** Remove all participants and winner */
  case object Clear extends Command

  /** Replies from RaffleActor, returned by server */
  sealed trait Reply

  /** Raffle state, contains list of participants and winner */
  case class State(participants: Map[String, ActorRef], winner: Option[String])
    extends Reply {

    def withParticipant(name: String, actor: ActorRef): State = copy(participants + (name -> actor))

    def withoutParticipant(name: String): State = copy(participants - name)

    def withWinner(name: String): State = copy(winner = Some(name))

    def withoutWinner(): State = copy(winner = None)

    def names: List[String] = participants.keys.toList.sorted

  }

  /** Participant entered the raffle */
  case class Entered(name: String) extends Reply

  /** Participant left the raffle */
  case class Left(name: String) extends Reply

  /** Error message */
  case class Error(error: String) extends Reply

  /** Actor handling Raffle server */
  class RaffleActor extends Actor with VaactorSession[State] {

    override val initialSessionState: State = State(Map.empty, None)

    /** Process received messages */
    override val sessionBehaviour: Receive = {
      case command: Command => command match {
        // Session wants to participate
        case Enter(name) =>
          // no name, reply with failure
          if (name.isEmpty)
            sender ! Error("Empty name not valid")
          // duplicate name, reply with failure
          else if (sessionState.participants.contains(name))
            sender ! Error(s"Name '$name' already participating")
          // add participant to raffle, notify participant, broadcast new state
          else {
            sessionState = sessionState.withParticipant(name, sender)
            sender ! Entered(name)
            broadcast(sessionState)
          }
        case Leave(name) =>
          if (sessionState.participants.contains(name)) {
            sessionState.participants(name) ! Left(name)
            sessionState = sessionState.withoutParticipant(name)
            broadcast(sessionState)
          }
        case SelectWinner =>
          if (sessionState.participants.nonEmpty) {
            val name = sessionState.names(Random.nextInt(sessionState.participants.size))
            sessionState = sessionState.withWinner(name)
            broadcast(sessionState)
          }
        case Clear =>
          for ((name, actor) <- sessionState.participants) actor ! Left(name)
          sessionState = sessionState.copy(Map.empty, None)
          broadcast(sessionState)
      }
    }

  }

}
