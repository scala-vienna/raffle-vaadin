package org.scala_vienna.raffle

import org.vaadin.addons.vaactor.VaactorServlet
import akka.actor.{ Actor, ActorRef, Props }
import scala.util.Random

object RaffleServer {

  /** Clients handled by raffle
    *
    * @param name  name of user
    * @param actor actorref for communication
    */
  case class Client(name: String, actor: ActorRef)

  /** Participant wants to enter raffle, processed by server
    *
    * @param client client of participant
    */
  case class Participate(client: Client)

  case class ParticipateSuccess(name: String)

  case class ParticipateFailure(error: String)

  case class Participants(participants: List[String])

  case object YouAreCoordinator

  case object StartRaffle

  case class Winner(name: String)

  /** ActoRef of raffle actor */
  val raffleServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "raffleServer")

  /** Actor handling chatroom */
  class ServerActor extends Actor {

    // List of participants
    private var participants = Map.empty[String, Client]

    /** Process received messages */
    def receive: Receive = {
      // Client wants to participate
      case Participate(client) =>
        // no name, reply with failure
        if (client.name.isEmpty)
          sender ! ParticipateFailure("Empty name not valid")
        // duplicate name, reply with failure
        else if (participants.contains(client.name))
          sender ! ParticipateFailure(s"Name '${ client.name }' already subscribed")
        // add client to raffle, broadcast new participant list to clients
        else {
          if (client.name == "stevan") {
            sender ! YouAreCoordinator
          }
          participants += client.name -> client
          broadcast(Participants(participants.keys.toList))
        }
      case StartRaffle => {
        val winner = Random.nextInt(participants.size)
        broadcast(Winner(participants.keys.toSeq(winner)))
      }
    }

    /** Send message to every client in chatroom
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = participants foreach { _._2.actor ! msg }

  }

}
