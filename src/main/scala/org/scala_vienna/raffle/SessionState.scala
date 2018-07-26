package org.scala_vienna.raffle

object SessionState {

  sealed trait State

  case class Participating(name: String) extends State

  case object Listening extends State

}
