name := "doobieleaktest"
version := "0.1"
scalaVersion := "2.12.11"

val DoobieVersion = "0.9.2"

libraryDependencies ++= Seq(

  "org.tpolecat" %% "doobie-core" % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres" % DoobieVersion,
  "org.tpolecat" %% "doobie-h2" % DoobieVersion,
  "org.tpolecat" %% "doobie-postgres-circe" % DoobieVersion,
  "org.tpolecat" %% "doobie-hikari" % DoobieVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.1",
)