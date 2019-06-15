name := "raffle-vaadin"

version := "2.0.0"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "vaadin-addons" at "http://maven.vaadin.com/vaadin-addons"
)

val vaadinVersion = "10.0.2"
val akkaVersion = "2.5.13"
val vaactorVersion = "2.0.0"

libraryDependencies ++= Seq(
  "org.vaadin.addons" % "vaactor" % vaactorVersion,
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.vaadin" % "vaadin-core" % vaadinVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  GraniteQRCodeGenerator.qrcode,
  "org.slf4j" % "slf4j-simple" % "1.7.25"
)

containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.3.21.v20170918" intransitive())

enablePlugins(JettyPlugin)

// Heroku needs a stage task because it calls "sbt compile stage"
val stage = taskKey[Unit]("Stage task")

val Stage = config("stage")

stage := {
  _root_.sbt.Keys.`package`.value
  (update in Stage).value.allFiles.foreach { f =>
    if (f.getName.matches("jetty-runner-.*jar")) {
      println("copying " + f.getName)
      IO.copyFile(f, baseDirectory.value / "target" / "jetty-runner.jar")
    }
  }
}
