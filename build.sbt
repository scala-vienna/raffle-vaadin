name := "vaadin-raffle"

version := "2.0.0"

scalaVersion := "2.12.6"

resolvers ++= Seq(
  "vaadin-addons" at "http://maven.vaadin.com/vaadin-addons"
)

val vaadinVersion = "10.0.1"
val akkaVersion = "2.5.13"
val vaactorVersion = "2.0.0"

libraryDependencies ++= Seq(
  "org.vaadin.addons" % "vaactor" % vaactorVersion,
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.vaadin" % "vaadin-core" % vaadinVersion,
  "com.typesafe.akka" %% "akka-actor" % akkaVersion
)

containerLibs in Jetty := Seq("org.eclipse.jetty" % "jetty-runner" % "9.3.21.v20170918" intransitive())

// Use more recent heroku-deploy lib due to authentication error with older version
herokuDeployLib := "com.heroku.sdk" % "heroku-deploy" % "2.0.4"

// Can be set here or on command line.
// To deploy to heroku on Windows use:
// sbt "set herokuAppName := ""<heroku app name>""" herokuDeploy
// Prerequisites: heroku cli is installed, heroku login has been called and the heroku app exists
herokuAppName := ""

enablePlugins(JettyPlugin)
enablePlugins(HerokuDeploy)
