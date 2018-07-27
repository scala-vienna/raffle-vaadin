package org.scala_vienna.raffle

import com.vaadin.flow.component.listbox.ListBox
import com.vaadin.flow.data.provider.{DataProvider, ListDataProvider}

import scala.collection.JavaConverters._

class NamePanel extends ListBox[String] {

  /** Contains list of raffle participants */
  val list = new java.util.ArrayList[String]()
  val dataProvider: ListDataProvider[String] = DataProvider.ofCollection[String](list)

  setDataProvider(dataProvider)

  def show(names: List[String]): Unit = {
    list.clear()
    list.addAll(names.asJava)
    dataProvider.refreshAll()
  }

}
