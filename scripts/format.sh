#!/bin/bash

# Ensure we're in the project root directory
cd "$(dirname "$0")/.."

# Run Google Java Format
./gradlew googleJavaFormat

# Check if formatting was successful
if [ $? -eq 0 ]; then
    echo "✓ Code formatting complete"
    exit 0
else
    echo "✗ Code formatting failed"
    exit 1
fi