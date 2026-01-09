#!/bin/bash

echo "============================================="
echo " Database Configuration Encryption Tool"
echo "============================================="
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven not found. Please install Maven."
    exit 1
fi

# Check if plain properties file exists
if [ ! -f "config/db_credentials.properties" ]; then
    echo "Error: config/db_credentials.properties not found"
    echo "Please create the file with your database credentials first."
    exit 1
fi

echo "Creating config directory if needed..."
mkdir -p config

echo ""
echo "Encrypting database configuration..."
mvn compile exec:java -q -Dexec.mainClass="com.the_pathfinders.util.EncryptedConfig" -Dexec.args="config/db_credentials.properties config/db.enc"

if [ $? -ne 0 ]; then
    echo ""
    echo "✗ Encryption failed!"
    exit 1
fi

echo ""
echo "============================================="
echo " Encryption Complete!"
echo "============================================="
echo ""
echo "Your database configuration has been encrypted to: config/db.enc"
echo ""
echo "IMPORTANT SECURITY STEPS:"
echo "1. Verify the encrypted file exists: config/db.enc"
echo "2. Set file permissions: chmod 600 config/db.enc"
echo "3. Consider deleting the plain text file: config/db_credentials.properties"
echo "4. Add config/db.enc to your .gitignore (already done)"
echo ""
echo "The application will now load credentials from the encrypted file."
echo "============================================="
