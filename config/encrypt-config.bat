@echo off
echo =============================================
echo  Database Configuration Encryption Tool
echo =============================================
echo.

REM Check if Maven is available
where mvn >nul 2>&1
if errorlevel 1 (
    echo Error: Maven not found. Please install Maven.
    pause
    exit /b 1
)

REM Check if plain properties file exists
if not exist "config\db_credentials.properties" (
    echo Error: config\db_credentials.properties not found
    echo Please create the file with your database credentials first.
    pause
    exit /b 1
)

echo Creating config directory if needed...
if not exist "config" mkdir config

echo.
echo Encrypting database configuration...
call mvn compile exec:java -q "-Dexec.mainClass=com.the_pathfinders.util.EncryptedConfig" "-Dexec.args=config/db_credentials.properties config/db.enc"

if errorlevel 1 (
    echo.
    echo ✗ Encryption failed!
    pause
    exit /b 1
)

echo.
echo =============================================
echo  Encryption Complete!
echo =============================================
echo.
echo Your database configuration has been encrypted to: config\db.enc
echo.
echo IMPORTANT SECURITY STEPS:
echo 1. Verify the encrypted file exists: config\db.enc
echo 2. Set file permissions (Windows): icacls config\db.enc /inheritance:r /grant:r "%USERNAME%":F
echo 3. Consider deleting the plain text file: config\db_credentials.properties
echo 4. Add config\db.enc to your .gitignore (already done)
echo.
echo The application will now load credentials from the encrypted file.
echo =============================================
pause
