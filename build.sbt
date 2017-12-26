lazy val akkaHttpVersion = "10.0.10"
lazy val akkaVersion    = "2.5.4"
lazy val jsoupVersion = "1.11.2"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "pl.edu.pw.elka",
      scalaVersion    := "2.12.3"
    )),
    name := "adoptimizer",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-xml"        % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "org.jsoup"         % "jsoup"                % jsoupVersion,

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3+"            % Test
    )
  )
