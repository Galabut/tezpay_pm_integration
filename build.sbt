name := """tezpay_pm_integration"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"
routesGenerator := InjectedRoutesGenerator
libraryDependencies ++= Seq(
  "com.typesafe.akka" % "akka-actor_2.11" % "2.4.0",
  "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.0",
  "com.rabbitmq" % "amqp-client" % "3.6.0",
  javaJdbc,
  cache,
  javaWs
)
