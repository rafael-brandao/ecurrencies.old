@echo off

set AKKA_HOME=%~dp0..
set JAVA_OPTS=@JAVA_OPTS@
set AKKA_CLASSPATH=%AKKA_HOME%\lib\scala-library-@scalaVersion@.jar;%AKKA_HOME%\config;%AKKA_HOME%\lib\akka\*

java %JAVA_OPTS% -cp "%AKKA_CLASSPATH%" -Dakka.home="%AKKA_HOME%" akka.kernel.Main %*