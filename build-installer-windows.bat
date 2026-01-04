@echo off
echo ============================================
echo  Shelter For Mind - Installer Builder
echo ============================================
echo.

echo Step 1: Cleaning previous builds...
call mvn clean
if errorlevel 1 goto error

echo.
echo Step 2: Building application...
call mvn package -DskipTests
if errorlevel 1 goto error

echo.
echo Step 3: Copying dependencies...
if not exist "target\libs" mkdir "target\libs"
call mvn dependency:copy-dependencies -DoutputDirectory=target\libs
if errorlevel 1 goto error

echo.
echo Step 4: Copying main JAR to libs folder...
copy /Y "target\shelter_of_mind-1.0.1.jar" "target\libs\"
if errorlevel 1 goto error

echo.
echo Step 5: Creating Windows MSI installer with full Java runtime...
call jpackage --input target\libs --name "Shelter for Mind" --main-jar shelter_of_mind-1.0.1.jar --main-class com.the_pathfinders.Launcher --type msi --app-version 1.0.1 --vendor "the_pathfinders" --description "Mental Health and Wellness Platform" --icon app-icon.ico --win-dir-chooser --win-menu --win-shortcut --java-options "-Dfile.encoding=UTF-8" --runtime-image "%JAVA_HOME%"
if errorlevel 1 goto error

echo.
echo ============================================
echo  Build Successful!
echo ============================================
echo.
echo Your installer is ready in the current directory:
dir /b *.msi 2>nul
if errorlevel 1 (
    echo Note: MSI file may be in a subdirectory
    dir /s /b *.msi
)
echo.
echo You can now distribute this installer!
echo ============================================
goto end

:error
echo.
echo ============================================
echo  Build Failed!
echo ============================================
echo.
echo Please check the error messages above.
echo Make sure you have:
echo  - JDK 21 or higher installed
echo  - Maven 3.6+ installed
echo  - All dependencies resolved
echo ============================================
pause
exit /b 1

:end
pause
