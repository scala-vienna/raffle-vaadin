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
  case class Participate(name: String)

  case class ParticipateSuccess(name: String)

  case class ParticipateFailure(error: String)

  case class Participants(participants: List[String])

  case object YouAreCoordinator

  case object StartRaffle

  case class Winner(name: String)

  case object RegisterClient

  case object Clear

  /** ActoRef of raffle actor */
  val raffleServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "raffleServer")

  /** Actor handling chatroom */
  class ServerActor extends Actor {

    // List of clients
    private var clients = Set.empty[ActorRef]

    // List of participants
    private var participants = List.empty[String]

    private var winner: Option[String] = None

    /** Process received messages */
    def receive: Receive = {
      // Client wants to register (for listening to messages broadcasted by the server)
      case RegisterClient =>
        clients += sender
        BroadcastParticipants()
        if (winner.isDefined) broadcast(Winner(winner.get))
      // Client wants to participate
      case Participate(name) =>
        // no name, reply with failure
        if (name.isEmpty)
          sender ! ParticipateFailure("Empty name not valid")
        // duplicate name, reply with failure
        else if (participants.contains(name))
          sender ! ParticipateFailure(s"Name '$name' already subscribed")
        // add client to raffle, broadcast new participant list to clients
        else {
          if (name == "raffle2018Coord") {
            sender ! YouAreCoordinator
          } else {
            participants :+= name
            BroadcastParticipants()
          }
        }
      case StartRaffle => {
        val winnerIndex = Random.nextInt(participants.size)
        winner = Some(participants(winnerIndex))
        broadcast(Winner(winner.get))
      }
      case Clear => {
        participants = List.empty[String]
        BroadcastParticipants()
      }
    }

    private def BroadcastParticipants() = broadcast(Participants(participants))

    /** Send message to every client in chatroom
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = clients foreach { _ ! msg }

  }

}
