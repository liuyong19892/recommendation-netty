@REM recommendation launcher script
@REM
@REM Environment:
@REM JAVA_HOME - location of a JDK home dir (optional if java on path)
@REM CFG_OPTS  - JVM options (optional)
@REM Configuration:
@REM RECOMMENDATION_config.txt found in the RECOMMENDATION_HOME.
@setlocal enabledelayedexpansion

@echo off

if "%RECOMMENDATION_HOME%"=="" set "RECOMMENDATION_HOME=%~dp0\\.."

set "APP_LIB_DIR=%RECOMMENDATION_HOME%\lib\"

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem FIRST we load the config file of extra options.
set "CFG_FILE=%RECOMMENDATION_HOME%\RECOMMENDATION_config.txt"
set CFG_OPTS=
if exist %CFG_FILE% (
  FOR /F "tokens=* eol=# usebackq delims=" %%i IN ("%CFG_FILE%") DO (
    set DO_NOT_REUSE_ME=%%i
    rem ZOMG (Part #2) WE use !! here to delay the expansion of
    rem CFG_OPTS, otherwise it remains "" for this loop.
    set CFG_OPTS=!CFG_OPTS! !DO_NOT_REUSE_ME!
  )
)

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)

if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo A Java JDK is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java JDK and install before running recommendation.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" and "javac.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)


rem We use the value of the JAVA_OPTS environment variable if defined, rather than the config.
set _JAVA_OPTS=%JAVA_OPTS%
if "!_JAVA_OPTS!"=="" set _JAVA_OPTS=!CFG_OPTS!

rem We keep in _JAVA_PARAMS all -J-prefixed and -D-prefixed arguments
rem "-J" is stripped, "-D" is left as is, and everything is appended to JAVA_OPTS
set _JAVA_PARAMS=
set _APP_ARGS=

:param_loop
call set _PARAM1=%%1
set "_TEST_PARAM=%~1"

if ["!_PARAM1!"]==[""] goto param_afterloop


rem ignore arguments that do not start with '-'
if "%_TEST_PARAM:~0,1%"=="-" goto param_java_check
set _APP_ARGS=!_APP_ARGS! !_PARAM1!
shift
goto param_loop

:param_java_check
if "!_TEST_PARAM:~0,2!"=="-J" (
  rem strip -J prefix
  set _JAVA_PARAMS=!_JAVA_PARAMS! !_TEST_PARAM:~2!
  shift
  goto param_loop
)

if "!_TEST_PARAM:~0,2!"=="-D" (
  rem test if this was double-quoted property "-Dprop=42"
  for /F "delims== tokens=1,*" %%G in ("!_TEST_PARAM!") DO (
    if not ["%%H"] == [""] (
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
    ) else if [%2] neq [] (
      rem it was a normal property: -Dprop=42 or -Drop="42"
      call set _PARAM1=%%1=%%2
      set _JAVA_PARAMS=!_JAVA_PARAMS! !_PARAM1!
      shift
    )
  )
) else (
  if "%1"=="-main" (
    set CUSTOM_MAIN_CLASS=%2
    shift
  ) else (
    set _APP_ARGS=!_APP_ARGS! !_PARAM1!
  )
)
shift
goto param_loop
:param_afterloop

set _JAVA_OPTS=!_JAVA_OPTS! !_JAVA_PARAMS!
:run
 
set "APP_CLASSPATH=%APP_LIB_DIR%\recommendation.recommendation-1.0.jar;%APP_LIB_DIR%\org.scala-lang.scala-library-2.11.8.jar;%APP_LIB_DIR%\io.netty.netty-all-4.1.13.Final.jar;%APP_LIB_DIR%\ch.qos.logback.logback-classic-1.1.7.jar;%APP_LIB_DIR%\ch.qos.logback.logback-core-1.1.7.jar;%APP_LIB_DIR%\org.slf4j.slf4j-api-1.7.20.jar;%APP_LIB_DIR%\log4j.log4j-1.2.17.jar;%APP_LIB_DIR%\org.jboss.marshalling.jboss-marshalling-1.4.10.Final.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-client-1.3.1.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-annotations-1.3.1.jar;%APP_LIB_DIR%\com.github.stephenc.findbugs.findbugs-annotations-1.3.9-1.jar;%APP_LIB_DIR%\junit.junit-4.12.jar;%APP_LIB_DIR%\org.hamcrest.hamcrest-core-1.3.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-protocol-1.3.1.jar;%APP_LIB_DIR%\com.google.protobuf.protobuf-java-2.5.0.jar;%APP_LIB_DIR%\commons-logging.commons-logging-1.2.jar;%APP_LIB_DIR%\commons-codec.commons-codec-1.9.jar;%APP_LIB_DIR%\commons-io.commons-io-2.4.jar;%APP_LIB_DIR%\commons-lang.commons-lang-2.6.jar;%APP_LIB_DIR%\com.google.guava.guava-12.0.1.jar;%APP_LIB_DIR%\org.apache.zookeeper.zookeeper-3.4.6.jar;%APP_LIB_DIR%\org.apache.htrace.htrace-core-3.1.0-incubating.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-mapper-asl-1.9.13.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-core-asl-1.9.13.jar;%APP_LIB_DIR%\org.jruby.jcodings.jcodings-1.0.8.jar;%APP_LIB_DIR%\org.jruby.joni.joni-2.1.2.jar;%APP_LIB_DIR%\com.yammer.metrics.metrics-core-2.2.0.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-common-1.3.1.jar;%APP_LIB_DIR%\commons-collections.commons-collections-3.2.2.jar;%APP_LIB_DIR%\org.mortbay.jetty.jetty-util-6.1.26.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-server-1.3.1.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-procedure-1.3.1.jar;%APP_LIB_DIR%\commons-httpclient.commons-httpclient-3.1.jar;%APP_LIB_DIR%\com.sun.jersey.jersey-core-1.9.jar;%APP_LIB_DIR%\com.sun.jersey.jersey-server-1.9.jar;%APP_LIB_DIR%\commons-cli.commons-cli-1.2.jar;%APP_LIB_DIR%\org.apache.commons.commons-math-2.2.jar;%APP_LIB_DIR%\org.mortbay.jetty.jetty-6.1.26.jar;%APP_LIB_DIR%\org.mortbay.jetty.jetty-sslengine-6.1.26.jar;%APP_LIB_DIR%\org.mortbay.jetty.jsp-2.1-6.1.14.jar;%APP_LIB_DIR%\org.mortbay.jetty.jsp-api-2.1-6.1.14.jar;%APP_LIB_DIR%\org.mortbay.jetty.servlet-api-2.5-6.1.14.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-jaxrs-1.9.13.jar;%APP_LIB_DIR%\tomcat.jasper-compiler-5.5.23.jar;%APP_LIB_DIR%\org.jamon.jamon-runtime-2.4.1.jar;%APP_LIB_DIR%\com.lmax.disruptor-3.3.0.jar;%APP_LIB_DIR%\org.apache.hbase.hbase-prefix-tree-1.3.1.jar;%APP_LIB_DIR%\tomcat.jasper-runtime-5.5.23.jar;%APP_LIB_DIR%\commons-el.commons-el-1.0.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-hdfs-2.7.3.jar;%APP_LIB_DIR%\commons-daemon.commons-daemon-1.0.13.jar;%APP_LIB_DIR%\javax.servlet.servlet-api-2.5.jar;%APP_LIB_DIR%\xmlenc.xmlenc-0.52.jar;%APP_LIB_DIR%\io.netty.netty-3.6.2.Final.jar;%APP_LIB_DIR%\xerces.xercesImpl-2.9.1.jar;%APP_LIB_DIR%\xml-apis.xml-apis-1.3.04.jar;%APP_LIB_DIR%\org.fusesource.leveldbjni.leveldbjni-all-1.8.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-client-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-common-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-annotations-2.7.3.jar;%APP_LIB_DIR%\org.apache.commons.commons-math3-3.1.1.jar;%APP_LIB_DIR%\commons-net.commons-net-3.1.jar;%APP_LIB_DIR%\com.sun.jersey.jersey-json-1.9.jar;%APP_LIB_DIR%\org.codehaus.jettison.jettison-1.1.jar;%APP_LIB_DIR%\com.sun.xml.bind.jaxb-impl-2.2.3-1.jar;%APP_LIB_DIR%\javax.xml.bind.jaxb-api-2.2.2.jar;%APP_LIB_DIR%\javax.xml.stream.stax-api-1.0-2.jar;%APP_LIB_DIR%\javax.activation.activation-1.1.jar;%APP_LIB_DIR%\net.java.dev.jets3t.jets3t-0.9.0.jar;%APP_LIB_DIR%\com.jamesmurty.utils.java-xmlbuilder-0.4.jar;%APP_LIB_DIR%\commons-configuration.commons-configuration-1.6.jar;%APP_LIB_DIR%\commons-digester.commons-digester-1.8.jar;%APP_LIB_DIR%\commons-beanutils.commons-beanutils-1.7.0.jar;%APP_LIB_DIR%\commons-beanutils.commons-beanutils-core-1.8.0.jar;%APP_LIB_DIR%\org.apache.avro.avro-1.7.4.jar;%APP_LIB_DIR%\com.thoughtworks.paranamer.paranamer-2.3.jar;%APP_LIB_DIR%\org.xerial.snappy.snappy-java-1.0.4.1.jar;%APP_LIB_DIR%\org.apache.commons.commons-compress-1.4.1.jar;%APP_LIB_DIR%\org.tukaani.xz-1.0.jar;%APP_LIB_DIR%\com.google.code.gson.gson-2.2.4.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-auth-2.7.3.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpclient-4.2.5.jar;%APP_LIB_DIR%\org.apache.httpcomponents.httpcore-4.2.5.jar;%APP_LIB_DIR%\org.apache.directory.server.apacheds-kerberos-codec-2.0.0-M15.jar;%APP_LIB_DIR%\org.apache.directory.server.apacheds-i18n-2.0.0-M15.jar;%APP_LIB_DIR%\org.apache.directory.api.api-asn1-api-1.0.0-M20.jar;%APP_LIB_DIR%\org.apache.directory.api.api-util-1.0.0-M20.jar;%APP_LIB_DIR%\org.apache.curator.curator-framework-2.7.1.jar;%APP_LIB_DIR%\org.apache.curator.curator-client-2.7.1.jar;%APP_LIB_DIR%\com.jcraft.jsch-0.1.42.jar;%APP_LIB_DIR%\org.apache.curator.curator-recipes-2.7.1.jar;%APP_LIB_DIR%\com.google.code.findbugs.jsr305-3.0.0.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-mapreduce-client-app-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-mapreduce-client-common-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-yarn-common-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-yarn-api-2.7.3.jar;%APP_LIB_DIR%\com.sun.jersey.jersey-client-1.9.jar;%APP_LIB_DIR%\org.codehaus.jackson.jackson-xc-1.9.13.jar;%APP_LIB_DIR%\com.google.inject.guice-3.0.jar;%APP_LIB_DIR%\javax.inject.javax.inject-1.jar;%APP_LIB_DIR%\aopalliance.aopalliance-1.0.jar;%APP_LIB_DIR%\org.sonatype.sisu.inject.cglib-2.2.1-v20090111.jar;%APP_LIB_DIR%\asm.asm-3.2.jar;%APP_LIB_DIR%\com.sun.jersey.contribs.jersey-guice-1.9.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-yarn-client-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-mapreduce-client-core-2.7.3.jar;%APP_LIB_DIR%\org.slf4j.slf4j-log4j12-1.7.10.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-yarn-server-common-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-mapreduce-client-shuffle-2.7.3.jar;%APP_LIB_DIR%\org.apache.hadoop.hadoop-mapreduce-client-jobclient-2.7.3.jar;%APP_LIB_DIR%\javax.servlet.jsp.jsp-api-2.1.jar;%APP_LIB_DIR%\com.typesafe.config-1.3.0.jar;%APP_LIB_DIR%\com.alibaba.fastjson-1.1.15.jar;%APP_LIB_DIR%\org.apache.commons.commons-lang3-3.4.jar"
set "APP_MAIN_CLASS=com.xmtj.netty.NettyServer"

if defined CUSTOM_MAIN_CLASS (
    set MAIN_CLASS=!CUSTOM_MAIN_CLASS!
) else (
    set MAIN_CLASS=!APP_MAIN_CLASS!
)

rem Call the application and pass all arguments unchanged.
"%_JAVACMD%" !_JAVA_OPTS! !RECOMMENDATION_OPTS! -cp "%APP_CLASSPATH%" %MAIN_CLASS% !_APP_ARGS!
if ERRORLEVEL 1 goto error
goto end

@endlocal


:end

exit /B %ERRORLEVEL%
