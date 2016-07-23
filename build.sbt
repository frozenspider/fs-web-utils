name         := "fs-web-utils"
version      := "0.3.1"
scalaVersion := "2.11.7"

EclipseKeys.withSource := true
EclipseKeys.createSrc  := EclipseCreateSrc.Default

libraryDependencies ++= Seq(
  "org.apache.httpcomponents" %  "httpclient"      % "4.5.1",
  "org.scala-lang.modules"    %% "scala-xml"       % "1.0.5",
  "org.ccil.cowan.tagsoup"    %  "tagsoup"         % "1.2.1",
  // Utility
  "commons-io"                %  "commons-io"      % "2.5",
  "commons-codec"             %  "commons-codec"   % "1.10"
)
