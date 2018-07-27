package org.scala_vienna.raffle

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.{ClickEvent, ComponentEventListener}

object ConfirmDialog {

  def apply(msg: String): ConfirmDialog = new ConfirmDialog(msg)

}

class ConfirmDialog private(msg: String) extends Dialog {

  val buttons = new HorizontalLayout()

  setCloseOnEsc(false)
  setCloseOnOutsideClick(false)

  add(new H2(msg), buttons)

  def addButton(text: String, clickListener: ComponentEventListener[ClickEvent[Button]]): this.type = {
    buttons.add(new Button(text, { event: ClickEvent[Button] =>
      clickListener.onComponentEvent(event)
      close()
    }))
    this
  }

  def onOK(clickListener: ComponentEventListener[ClickEvent[Button]]): this.type = addButton("OK", clickListener)

  def onCancel(clickListener: ComponentEventListener[ClickEvent[Button]]): this.type = addButton("Cancel", clickListener)

}
