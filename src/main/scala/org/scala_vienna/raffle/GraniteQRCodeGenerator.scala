package org.scala_vienna.raffle

import com.vaadin.flow.component.dependency.HtmlImport
import com.vaadin.flow.component.{Component, PropertyDescriptor, PropertyDescriptors, Tag}
import org.scala_vienna.raffle.GraniteQRCodeGenerator._

/** Scala/Vaadin-Flow API for web component Granite QRCode Generator
  *
  * https://www.webcomponents.org/element/LostInBrittany/granite-qrcode-generator
  *
  * https://lostinbrittany.github.io/granite-qrcode-generator/components/granite-qrcode-generator/
  *
  */
object GraniteQRCodeGenerator {

  type JBoolean = java.lang.Boolean

  sealed trait Mode {
    val attr: String
  }

  case object Numeric extends Mode {
    val attr = "numeric"
  }

  case object Alphanumeric extends Mode {
    val attr = "alphanumeric"
  }

  case object Octet extends Mode {
    val attr = "octet"
  }

  sealed trait EccLevel {
    val attr: String
  }

  case object Low extends EccLevel {
    val attr = "L"
  }

  case object Medium extends EccLevel {
    val attr = "M"
  }

  case object Quartile extends EccLevel {
    val attr = "Q"
  }

  case object High extends EccLevel {
    val attr = "H"
  }

  val autoProperty: PropertyDescriptor[JBoolean, JBoolean] =
    PropertyDescriptors.propertyWithDefault("auto", false)

  val dataProperty: PropertyDescriptor[String, String] =
    PropertyDescriptors.propertyWithDefault("data", "")

  val ecclevelProperty: PropertyDescriptor[String, String] =
    PropertyDescriptors.propertyWithDefault("ecclevel", Low.attr)

  val marginProperty: PropertyDescriptor[Integer, Integer] =
    PropertyDescriptors.propertyWithDefault("margin", 4)

  val maskProperty: PropertyDescriptor[Integer, Integer] =
    PropertyDescriptors.propertyWithDefault("mask", -1)

  val modeProperty: PropertyDescriptor[String, String] =
    PropertyDescriptors.propertyWithDefault("mode", Numeric.attr)

  val modulesizeProperty: PropertyDescriptor[Integer, Integer] =
    PropertyDescriptors.propertyWithDefault("modulesize", 5)

  val versionProperty: PropertyDescriptor[Integer, Integer] =
    PropertyDescriptors.propertyWithDefault("version", -1)

}

@Tag("granite-qrcode-generator")
@HtmlImport("bower_components/granite-qrcode-generator/granite-qrcode-generator.html")
class GraniteQRCodeGenerator extends Component {

  def this(data: String) {
    this()
    auto = true
    this.data = data
  }

  // property auto
  def auto: Boolean = autoProperty.get(this)

  def auto_=(v: Boolean): Unit = autoProperty.set(this, v)

  def withAuto(v: Boolean): this.type = {
    auto = v
    this
  }

  // property data
  def data: String = dataProperty.get(this)

  def data_=(v: String): Unit = dataProperty.set(this, v)

  def withData(v: String): this.type = {
    data = v
    this
  }

  // property ecclevel
  def ecclevel: EccLevel = ecclevelProperty.get(this) match {
    case Low.attr => Low
    case Medium.attr => Medium
    case Quartile.attr => Quartile
    case High.attr => High
    case _ => Low
  }

  def ecclevel_=(v: EccLevel): Unit = ecclevelProperty.set(this, v.attr)

  def withEcclevel(v: EccLevel): this.type = {
    ecclevel = v
    this
  }

  // property margin
  def margin: Int = marginProperty.get(this)

  def margin_=(v: Int): Unit =
    if (v < 4)
      throw new IllegalArgumentException(s"Margin $v lower than minimum value of 4")
    else marginProperty.set(this, v)

  def withMargin(v: Int): this.type = {
    margin = v
    this
  }

  // property mask
  def mask: Int = maskProperty.get(this)

  def mask_=(v: Int): Unit =
    if (v < -1 || v == 0 || v > 7)
      throw new IllegalArgumentException(s"Mask $v must be between 1 and 7 (or -1)")
    else maskProperty.set(this, v)

  def withMask(v: Int): this.type = {
    mask = v
    this
  }

  // property mode
  def mode: Mode = modeProperty.get(this) match {
    case Numeric.attr => Numeric
    case Alphanumeric.attr => Alphanumeric
    case Octet.attr => Octet
    case _ => Numeric
  }

  def mode_=(v: Mode): Unit = modeProperty.set(this, v.attr)

  def withMode(v: Mode): this.type = {
    mode = v
    this
  }

  // property modulesize
  def modulesize: Int = modulesizeProperty.get(this)

  def modulesize_=(v: Int): Unit =
    if (v <= 0)
      throw new IllegalArgumentException(s"Modulesize $v must be positive")
    else modulesizeProperty.set(this, v)

  def withModulesize(v: Int): this.type = {
    modulesize = v
    this
  }

  // property version
  def version: Int = versionProperty.get(this)

  def version_=(v: Int): Unit =
    if (v < -1 || v == 0 || v > 40)
      throw new IllegalArgumentException(s"Version $v must be between 1 and 40 (or -1)")
    else versionProperty.set(this, v)

  def withVersion(v: Int): this.type = {
    version = v
    this
  }

}
