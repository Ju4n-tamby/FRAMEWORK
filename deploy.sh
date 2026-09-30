#!/bin/bash

APP_NAME="FRAMEWORK"
SRC_DIR="src/main/java"
BUILD_DIR="build"
LIB_DIR="lib"
SERVLET_API_JAR="$LIB_DIR/servlet-api.jar"
DEST="/home/juan/Documents/Framework/TEST/lib"

# Nettoyage
if [ -d "$BUILD_DIR" ]; then
    rm -rf "$BUILD_DIR"
fi
mkdir -p "$BUILD_DIR"

# Compilation
find "$SRC_DIR" -name "*.java" > sources.txt
javac -cp "$SERVLET_API_JAR" -d "$BUILD_DIR" @sources.txt
COMPILE_STATUS=$?
rm -f sources.txt

if [ $COMPILE_STATUS -ne 0 ]; then
    echo "ERREUR : Compilation échouée."
    read -p "Appuyez sur une touche pour continuer..."
    exit 1
fi

# Création du JAR
jar -cvf "$APP_NAME.jar" -C "$BUILD_DIR" .

if [ $? -ne 0 ]; then
    echo "ERREUR : Création du JAR échouée."
    read -p "Appuyez sur une touche pour continuer..."
    exit 1
fi

echo "JAR créé avec succès."

# Copie vers le projet TEST
cp "$APP_NAME.jar" "$DEST"
echo "JAR copié vers $DEST"

read -p "Appuyez sur une touche pour continuer..."