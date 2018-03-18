package org.scala_vienna.raffle

import org.vaadin.addons.vaactor.VaactorServlet

import akka.actor.{ Actor, ActorRef, Props }

import scala.util.Random

object RaffleServer {

  /** Clients handled by chat room
    *
    * @param name  name of user
    * @param actor actorref for communication
    */
  case class Client(name: String, actor: ActorRef)

  /** Subscribe client to chatroom, processed by chatroom
    *
    * @param client enters chatroom
    */
  case class Subscribe(client: Client)

  case class SubscriptionSuccess(name: String)

  case class SubscriptionFailure(error: String)

  case class SubscriptionCancelled(name: String)

  case class Participants(names: Seq[String])

  case class Enter(participants: List[String])

  case object YouAreCoordinator

  case object StartRaffle

  case class Result(name: String)


  case object RequestMembers

  /** ActoRef of chatroom actor */
  val raffleServer: ActorRef = VaactorServlet.system.actorOf(Props[ServerActor], "raffleServer")

  /** Actor handling chatroom */
  class ServerActor extends Actor {

    // List of participants
    private var participants = Map.empty[String, Client]

    /** Process received messages */
    def receive: Receive = {
      // Subscribe from client
      case Subscribe(client) =>
        // no name, reply with failure
        if (client.name.isEmpty)
          sender ! SubscriptionFailure("Empty name not valid")
        // duplicate name, reply with failure
        else if (participants.contains(client.name))
          sender ! SubscriptionFailure(s"Name '${ client.name }' already subscribed")
        // add client to chatroom, reply with success, brodcast Enter to clients
        else {
          if (client.name == "stevan") {
            sender ! YouAreCoordinator
          }
          participants += client.name -> client
          //sender ! SubscriptionSuccess(client.name)
          broadcast(Enter(participants.keys.toList))
        }
      case StartRaffle => {
        val winner = Random.nextInt(participants.size)
        broadcast(Result(participants.keys.toSeq(winner)))
      }
      // RequestMembers from client, send member list to sending client
      //case RequestMembers =>
      //  sender ! Participants(participants.keySet.toList)
    }

    /** Send message to every client in chatroom
      *
      * @param msg message
      */
    def broadcast(msg: Any): Unit = participants foreach { _._2.actor ! msg }

  }

}
