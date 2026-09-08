#!/bin/zsh
set -euo pipefail
cd "$(dirname "$0")"

echo "Building Time Off Tracker..."
mvn clean package

mkdir -p dist
rm -rf "dist/Time Off Tracker.app"

ICON_PATH="$(zsh scripts/build-macos-icon.sh)"
APP_VERSION="$(python3 - <<'PY'
import xml.etree.ElementTree as ET
root = ET.parse('pom.xml').getroot()
ns = {'m': 'http://maven.apache.org/POM/4.0.0'}
print(root.find('m:version', ns).text)
PY
)"

echo "Packaging macOS application..."
jpackage \
  --type app-image \
  --name "Time Off Tracker" \
  --app-version "$APP_VERSION" \
  --vendor "Jonathan Stotts" \
  --mac-package-identifier "com.jonbstotts.timeofftracker" \
  --input target \
  --main-jar time-off-tracker.jar \
  --icon "$ICON_PATH" \
  --dest dist

echo
echo "Build complete: dist/Time Off Tracker.app"
echo "Install the .app bundle in Applications. Do not install the target/*.jar file."
open -R "dist/Time Off Tracker.app"
read -k 1 "?Press any key to close this window..."
