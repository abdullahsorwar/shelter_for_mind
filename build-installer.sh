#!/bin/bash

echo "============================================"
echo " Shelter For Mind - Installer Builder"
echo "============================================"
echo ""

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check prerequisites
echo "Checking prerequisites..."
if ! command_exists mvn; then
    echo "Error: Maven not found. Please install Maven 3.6+"
    exit 1
fi

if ! command_exists java; then
    echo "Error: Java not found. Please install JDK 21+"
    exit 1
fi

echo "Prerequisites OK"
echo ""

# Detect platform
PLATFORM=$(uname -s)
case "${PLATFORM}" in
    Linux*)     
        echo "Detected platform: Linux"
        INSTALLER_TYPE="deb"
        ;;
    Darwin*)    
        echo "Detected platform: macOS"
        INSTALLER_TYPE="dmg"
        ;;
    *)          
        echo "Unknown platform: ${PLATFORM}"
        exit 1
        ;;
esac

echo "Will create ${INSTALLER_TYPE} installer"
echo ""

# Step 1: Clean
echo "Step 1: Cleaning previous builds..."
mvn clean
if [ $? -ne 0 ]; then
    echo "Clean failed!"
    exit 1
fi

# Step 2: Build
echo ""
echo "Step 2: Building application..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

# Step 3: Copy dependencies
echo ""
echo "Step 3: Copying dependencies..."
mkdir -p target/libs
mvn dependency:copy-dependencies -DoutputDirectory=target/libs
if [ $? -ne 0 ]; then
    echo "Dependency copy failed!"
    exit 1
fi

# Step 4: Copy main JAR
echo ""
echo "Step 4: Copying main JAR to libs folder..."
cp target/shelter_of_mind-1.0.1.jar target/libs/
if [ $? -ne 0 ]; then
    echo "JAR copy failed!"
    exit 1
fi

# Step 5: Create installer
echo ""
echo "Step 5: Creating ${INSTALLER_TYPE} installer with full Java runtime..."
if [ "${INSTALLER_TYPE}" = "dmg" ]; then
    jpackage --input target/libs --name "Shelter for Mind" --main-jar shelter_of_mind-1.0.1.jar --main-class com.the_pathfinders.Launcher --type dmg --app-version 1.0.1 --vendor "the_pathfinders" --description "Mental Health and Wellness Platform" --icon logo_testing.png --mac-package-name "Shelter for Mind" --java-options "-Dfile.encoding=UTF-8" --runtime-image "$JAVA_HOME"
else
    jpackage --input target/libs --name "shelter-for-mind" --main-jar shelter_of_mind-1.0.1.jar --main-class com.the_pathfinders.Launcher --type deb --app-version 1.0.1 --vendor "the_pathfinders" --description "Mental Health and Wellness Platform" --icon logo_testing.png --linux-shortcut --java-options "-Dfile.encoding=UTF-8" --runtime-image "$JAVA_HOME"
fi
if [ $? -ne 0 ]; then
    echo "JPackage failed!"
    exit 1
fi

# Success
echo ""
echo "============================================"
echo " Build Successful!"
echo "============================================"
echo ""
echo "Your installer is ready in current directory:"
echo ""
if [ "${INSTALLER_TYPE}" = "dmg" ]; then
    ls -lh *.dmg 2>/dev/null || echo "DMG file created"
else
    ls -lh *.deb 2>/dev/null || echo "DEB file created"
fi
echo ""
echo "You can now distribute this installer!"
echo "============================================"
