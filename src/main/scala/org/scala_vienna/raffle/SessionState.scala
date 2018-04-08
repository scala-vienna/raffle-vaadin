package org.scala_vienna.raffle

object SessionState {
  sealed trait State
  case class Participating(name: String) extends State
  case object Coordinator extends State
  case object None extends State
}
