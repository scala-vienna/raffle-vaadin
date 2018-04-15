name := "vaadin-raffle"

version := "0.1"

scalaVersion := "2.12.4"

resolvers ++= Seq(
  "vaadin-addons" at "http://maven.vaadin.com/vaadin-addons"
)

val vaadinVersion = "8.3.1"
val akkaVersion = "2.5.11"
libraryDependencies ++= Seq(
  "org.vaadin.addons" % "vaactor" % "1.0.2",
  "javax.servlet" % "javax.servlet-api" % "3.1.0" % "provided",
  "com.vaadin" % "vaadin-server" % vaadinVersion,
  "com.vaadin" % "vaadin-client-compiled" % vaadinVersion,
  "com.vaadin" % "vaadin-themes" % vaadinVersion,
  "com.vaadin" % "vaadin-push" % vaadinVersion,
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
