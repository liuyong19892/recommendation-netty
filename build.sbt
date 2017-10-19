
import com.typesafe.sbt.SbtNativePackager.{packageArchetype, _}
import com.typesafe.sbt.packager.Keys._
name := "recommendation"

version := "1.0"

scalaVersion := "2.11.8"

mainClass in Compile := Some("com.xmtj.netty.NettyServer")

crossScalaVersions := Seq("2.11.8")

packageArchetype.java_server

packageSummary in Linux := "netty start"

packageDescription := "netty start"

bashScriptConfigLocation := Some("/services/data/recommendation/conf/jvmopts")

bashScriptExtraDefines += """addJava "-Dconfig.file=/services/data/recommendation/conf/application.conf""""

mappings in Universal <++= sourceDirectory map { src =>
  val confDir = src / "main" / "resources"
  confDir.*** pair rebase(confDir, "conf") filterNot(fn => fn._2.contains("app-local.conf"))
}
val hadoopVersion = "2.7.3"

libraryDependencies ++= Seq(
//"com.google.guava" % "guava" % "18.0",
 //"io.netty" % "netty-all" % "5.0.0.Alpha2",
  "io.netty" % "netty-all" % "4.1.13.Final",
"ch.qos.logback" % "logback-classic" % "1.1.7",
"log4j" % "log4j" % "1.2.17",
"org.jboss.marshalling" % "jboss-marshalling" % "1.4.10.Final"
  ,    "org.apache.hbase" % "hbase-client" % "1.3.1"
  ,    "org.apache.hbase" % "hbase-common" % "1.3.1"
  ,    "org.apache.hbase" % "hbase-server" % "1.3.1"

  ,    "org.apache.hadoop" % "hadoop-hdfs" % hadoopVersion
  ,    "org.apache.hadoop" % "hadoop-client" % hadoopVersion
  ,    "org.apache.hadoop" % "hadoop-common" % hadoopVersion
  ,
  "com.typesafe"     % "config"       % "1.3.0",
  "com.alibaba" % "fastjson" % "1.1.15",
"org.apache.commons" % "commons-lang3" % "3.4"
)



javacOptions ++= Seq("-source", "1.8", "-target", "1.8","-encoding", "UTF-8" )
