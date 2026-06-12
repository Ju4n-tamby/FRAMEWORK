@echo off

set APP_NAME=FRAMEWORK
set SRC_DIR=src\main\java
set BUILD_DIR=build
set LIB_DIR=lib
set SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar

REM Nettoyage
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
mkdir %BUILD_DIR%

REM Compilation
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp %SERVLET_API_JAR% -d %BUILD_DIR% @sources.txt
del sources.txt

REM Création du JAR
jar -cvf %APP_NAME%.jar -C %BUILD_DIR% .

echo JAR cree avec succes.
pause