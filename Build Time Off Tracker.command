#!/bin/zsh
set -euo pipefail
cd "$(dirname "$0")"

echo "Building Time Off Tracker..."
mvn clean package

mkdir -p dist
rm -rf "dist/Time Off Tracker.app"

ICON_PATH="$(zsh scripts/build-macos-icon.sh)"

echo "Packaging macOS application..."
jpackage \
  --type app-image \
  --name "Time Off Tracker" \
  --input target \
  --main-jar time-off-tracker.jar \
  --icon "$ICON_PATH" \
  --dest dist

echo
echo "Build complete: dist/Time Off Tracker.app"
open dist
read -k 1 "?Press any key to close this window..."
