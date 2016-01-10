name         := "scala-web-utils"
version      := "0.1"
scalaVersion := "2.11.7"

EclipseKeys.withSource := true
EclipseKeys.createSrc  := EclipseCreateSrc.Default

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" %  "httpclient"      % "4.5.1",
  "org.scala-lang.modules"    %% "scala-xml"       % "1.0.5",
  "org.ccil.cowan.tagsoup"    %  "tagsoup"         % "1.2.1"
)
