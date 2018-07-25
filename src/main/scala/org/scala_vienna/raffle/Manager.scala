package org.scala_vienna.raffle

import akka.actor.{Actor, ActorRef, Props}
import org.vaadin.addons.vaactor.{VaactorServlet, loadedConfig}

import scala.annotation.tailrec
import scala.util.Random

/** Manager for all raffles */
object Manager {

  sealed trait Command

  case object Create extends Command

  case class Lookup(id: String) extends Command

  case class Close(id: String) extends Command

  sealed trait Reply

  case class Raffle(id: String, actor: ActorRef) extends Reply {
    def !(msg: Any)(implicit sender: ActorRef): Unit = actor.tell(msg, sender)
  }

  case class Closed(id: String) extends Reply

  case class Error(msg: String) extends Reply

  val actor: ActorRef = VaactorServlet.system.actorOf(Props[ManagerActor],
    loadedConfig.getString("raffle.manager-name"))

  def !(msg: Any)(implicit sender: ActorRef): Unit = actor.tell(msg, sender)

  private class ManagerActor extends Actor {

    //noinspection ActorMutableStateInspection
    // active raffles
    private var raffles = Map.empty[String, Raffle]

    @tailrec
    private def unusedId(): String = {
      val id = createId()
      if (!(raffles contains id))
        id
      else
        unusedId()
    }

    override def receive: Receive = {
      case command: Command => command match {
        case Create =>
          val id = unusedId()
          val actor = context.actorOf(Props[RaffleServer.ServerActor], s"raffle-$id")
          val raffle = Raffle(id, actor)
          raffles += (id -> raffle)
          sender ! raffle
        case Lookup(id) =>
          raffles.get(id) match {
            case Some(raffle) => sender ! raffle
            case None => sender ! Error(s"no raffle with id $id active")
          }
        case Close(id) =>
          // todo - send Shutdown to RaffleServer
          raffles -= id
          sender ! Closed(id)
      }
    }

  }

  private val allowedChars: Seq[Char] = ('A' to 'H') ++ ('J' to 'N') ++ ('P' to 'Z') ++ ('0' to '9')

  private def createId(): String =
    for (_ <- "1234") yield allowedChars(Random.nextInt(allowedChars.size))
}
