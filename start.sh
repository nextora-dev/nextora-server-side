#!/bin/bash

# Load environment variables from .env file
# This script properly parses .env files by filtering out comments and empty lines
set -a
if [ -f .env ]; then
    while IFS='=' read -r key value; do
        # Skip empty lines and comments
        if [[ -n "$key" && "$key" != \#* ]]; then
            # Remove leading/trailing whitespace
            key=$(echo "$key" | xargs)
            value=$(echo "$value" | xargs)
            # Export the variable
            export "$key"="$value"
        fi
    done < .env
fi
set +a

# Kill any existing Java processes
pkill java || true

# Wait a moment for graceful shutdown
sleep 2

# Find and run the JAR file
JAR_FILE=$(find . -name "*.jar" -type f | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "Error: No JAR file found in current directory"
    exit 1
fi

echo "Starting application with JAR: $JAR_FILE"
echo "Database Host: $RDS_HOST"
echo "Database Name: $RDS_DB_NAME"

# Start the Spring Boot application with environment variables
nohup java \
    -Dspring.datasource.url="jdbc:postgresql://${RDS_HOST}:5432/${RDS_DB_NAME}" \
    -Dspring.datasource.username="${RDS_USERNAME}" \
    -Dspring.datasource.password="${RDS_PASSWORD}" \
    -Dspring.profiles.active=prod \
    -jar "$JAR_FILE" > app.log 2>&1 &

echo "Application started with PID $!"

