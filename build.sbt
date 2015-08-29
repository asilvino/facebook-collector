name := """facebook-collector"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  cache,
  javaWs,
  filters,
  "org.mongodb" % "mongo-java-driver" % "2.13.0",
  "org.springframework" % "spring-core" % "4.1.6.RELEASE",
  "org.springframework" % "spring-context" % "4.1.6.RELEASE",
  "org.springframework.data" % "spring-data-mongodb" % "1.7.2.RELEASE",
  "org.apache.httpcomponents" % "httpclient" % "4.4.1",
  "org.springframework.social" % "spring-social-facebook" % "2.0.1.RELEASE",
  "com.newrelic.agent.java" % "newrelic-api" % "3.15.0",
  "org.webjars" % "requirejs" % "2.1.14-1",
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "org.webjars" % "jquery" % "1.11.1",
  "org.webjars" % "bootstrap" % "3.3.5" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.4.3" exclude("org.webjars", "jquery"),
  "org.webjars.bower" % "angular-resource" % "1.4.3",
  "org.webjars" % "angular-chosen" % "1.0.6",
  "org.webjars.bower" % "angular-block-ui" % "0.2.0",
  "com.sachinhandiekar" % "jInstagram" % "1.1.3"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
