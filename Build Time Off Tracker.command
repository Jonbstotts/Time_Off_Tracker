#!/bin/zsh
set -e
cd "$(dirname "$0")"
echo "Building Time Off Tracker..."
mvn clean package
mkdir -p dist
rm -rf "dist/Time Off Tracker.app"
jpackage \
  --type app-image \
  --name "Time Off Tracker" \
  --input target \
  --main-jar time-off-tracker.jar \
  --dest dist
echo
echo "Build complete: dist/Time Off Tracker.app"
open dist
read -k 1 "?Press any key to close this window..."
