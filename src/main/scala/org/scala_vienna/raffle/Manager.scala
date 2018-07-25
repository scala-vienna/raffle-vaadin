package org.scala_vienna.raffle

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props}
import org.vaadin.addons.vaactor.{VaactorServlet, loadedConfig}

import scala.annotation.tailrec
import scala.util.Random

/** Manager for all raffles */
object Manager {

  sealed trait Command

  case object Create extends Command

  case class Lookup(id: String) extends Command

  case class LookupKey(key: String) extends Command

  case class Close(id: String) extends Command

  sealed trait Reply

  case class Raffle(id: String, actor: ActorRef) extends Reply {
    def !(msg: Any)(implicit sender: ActorRef): Unit = actor.tell(msg, sender)
  }

  case class Closed(id: String) extends Reply

  case class Error(msg: String) extends Reply

  case class RaffleWithKey(raffle: Raffle, key: String)

  val actor: ActorRef = VaactorServlet.system.actorOf(Props[ManagerActor],
    loadedConfig.getString("raffle.manager-name"))

  def !(msg: Any)(implicit sender: ActorRef): Unit = actor.tell(msg, sender)

  private class ManagerActor extends Actor {

    //noinspection ActorMutableStateInspection
    // active raffles
    private var rafflesById = Map.empty[String, RaffleWithKey]

    //noinspection ActorMutableStateInspection
    // active raffles with key
    private var rafflesByKey = Map.empty[String, RaffleWithKey]

    @tailrec
    private def unusedId(): String = {
      val id = createId()
      if (!(rafflesById contains id))
        id
      else
        unusedId()
    }

    def createRaffle(): RaffleWithKey = {
      val id = unusedId()
      val key = UUID.randomUUID().toString
      val actor = context.actorOf(Props[RaffleServer.ServerActor], s"raffle-$id")
      val raffle = Raffle(id, actor)
      val raffleWithKey = RaffleWithKey(raffle, key)
      rafflesById += (id -> raffleWithKey)
      rafflesByKey += (key -> raffleWithKey)
      raffleWithKey
    }

    def closeRaffle(id: String): Unit =
      for (r <- rafflesById.get(id)) {
        rafflesById -= r.raffle.id
        rafflesByKey -= r.key
      }

    override def receive: Receive = {
      case command: Command => command match {
        case Create =>
          sender ! createRaffle()
        case Lookup(id) =>
          rafflesById.get(id) match {
            case Some(raffleWithKey) => sender ! raffleWithKey.raffle
            case None => sender ! Error(s"no raffle with id $id active")
          }
        case LookupKey(key) =>
          rafflesByKey.get(key) match {
            case Some(raffleWithKey) => sender ! raffleWithKey.raffle
            case None => sender ! Error(s"no raffle with key $key active")
          }
        case Close(id) =>
          // todo - send Shutdown to RaffleServer
          closeRaffle(id)
          sender ! Closed(id)
      }
    }

  }

  private val allowedChars: Seq[Char] = ('A' to 'H') ++ ('J' to 'N') ++ ('P' to 'Z') ++ ('0' to '9')

  private def createId(): String =
    for (_ <- "1234") yield allowedChars(Random.nextInt(allowedChars.size))
}
