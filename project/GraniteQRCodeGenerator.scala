import sbt._

object GraniteQRCodeGenerator {

  val qrcodeVersion = "1.1.4"

  /** Granite QRCode Generator
    *
    * https://www.webcomponents.org/element/LostInBrittany/granite-qrcode-generator
    *
    * https://mvnrepository.com/artifact/org.webjars.bowergithub.lostinbrittany/granite-qrcode-generator/1.1.4
    *
    * https://lostinbrittany.github.io/granite-qrcode-generator/components/granite-qrcode-generator/
    *
    * intransitive, because of the reference to org.webjars.bowergithub.lifthrasiir » qr.js	[0.0.20110119...
    * is not available in repositories
    *
    * Content manually copied to frontend/bower_components/qrjs
    *
    */

  val qrcode = "org.webjars.bowergithub.lostinbrittany" % "granite-qrcode-generator" % qrcodeVersion intransitive()

}
