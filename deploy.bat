@echo off

set APP_NAME=FRAMEWORK
set SRC_DIR=src\main\java
set BUILD_DIR=build
set LIB_DIR=lib
set SERVLET_API_JAR=%LIB_DIR%\servlet-api.jar
set DEST=D:\Cours_S4\WEB\FRAMEWORK\TEST\lib\

REM Nettoyage
if exist %BUILD_DIR% rmdir /s /q %BUILD_DIR%
mkdir %BUILD_DIR%

REM Compilation
dir /s /b %SRC_DIR%\*.java > sources.txt
javac -cp %SERVLET_API_JAR% -d %BUILD_DIR% @sources.txt
del sources.txt

if %ERRORLEVEL% neq 0 (
    echo ERREUR : Compilation échouée.
    pause
    exit /b 1
)

REM Création du JAR
jar -cvf %APP_NAME%.jar -C %BUILD_DIR% .

if %ERRORLEVEL% neq 0 (
    echo ERREUR : Création du JAR échouée.
    pause
    exit /b 1
)

echo JAR créé avec succès.

REM Copie vers le projet TEST
copy %APP_NAME%.jar %DEST%
echo JAR copié vers %DEST%

pause