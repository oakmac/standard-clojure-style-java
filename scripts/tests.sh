#!/bin/bash

# Ensure we're in the project root directory
cd "$(dirname "$0")/.."

# run the unit tests
./gradlew test --info
