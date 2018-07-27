package org.scala_vienna.raffle

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.H2
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.{ClickEvent, Component, ComponentEventListener}

object ConfirmDialog {

  def apply(content: Component): ConfirmDialog = new ConfirmDialog(content)

  def apply(msg: String): ConfirmDialog = new ConfirmDialog(new H2(msg))

}

class ConfirmDialog private(content: Component) extends Dialog {

  val buttons = new HorizontalLayout()

  setCloseOnEsc(false)
  setCloseOnOutsideClick(false)

  add(content, buttons)

  def withButton(text: String, clickListener: ComponentEventListener[ClickEvent[Button]]): this.type = {
    buttons.add(new Button(text, { event: ClickEvent[Button] =>
      clickListener.onComponentEvent(event)
      close()
    }))
    this
  }

  def onOK(clickListener: ComponentEventListener[ClickEvent[Button]]): this.type =
    withButton("OK", clickListener)

  def onCancel(clickListener: ComponentEventListener[ClickEvent[Button]]): this.type =
    withButton("Cancel", clickListener)

}
