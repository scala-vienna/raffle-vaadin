package org.scala_vienna.raffle

import org.scala_vienna.raffle.DelayedValue._

object DelayedValue {

  object ValueAlreadySet extends Exception("value must not be set twice")

  object ValueNotYetSet extends Exception("value must be set before first read")

  def apply[T]: DelayedValue[T] = new DelayedValue[T]()

}

class DelayedValue[T] private {

  private var _value: Option[T] = None

  def value: T = _value match {
    case Some(v) => v
    case None => throw ValueNotYetSet
  }

  def value_=(v: T): Unit = _value match {
    case None => _value = Some(v)
    case Some(_) => throw ValueAlreadySet
  }

}
