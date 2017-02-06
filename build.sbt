name         := "fs-web-utils"
version      := "0.4"
scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.12.1")

EclipseKeys.withSource := true
EclipseKeys.createSrc  := EclipseCreateSrc.Default

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" %  "httpclient"      % "4.5.3",
  "org.scala-lang.modules"    %% "scala-xml"       % "1.0.5",
  "org.ccil.cowan.tagsoup"    %  "tagsoup"         % "1.2.1",
  // Utility
  "commons-io"                %  "commons-io"      % "2.5",
  "commons-codec"             %  "commons-codec"   % "1.10"
)
