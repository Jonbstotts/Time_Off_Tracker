#!/bin/zsh
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
ASSET="$ROOT/assets/time-off-tracker-icon.png.b64"
BUILD_DIR="$ROOT/build/macos-icon"
SOURCE_PNG="$BUILD_DIR/time-off-tracker-icon-1024.png"
ICONSET="$BUILD_DIR/TimeOffTracker.iconset"
ICNS="$BUILD_DIR/TimeOffTracker.icns"

if [[ ! -f "$ASSET" ]]; then
  echo "Missing icon asset: $ASSET" >&2
  exit 1
fi

rm -rf "$BUILD_DIR"
mkdir -p "$ICONSET"

# macOS /usr/bin/base64 expects input/output paths through -i/-o.
# Using a positional input path prints the usage text and aborts the build.
/usr/bin/base64 -D -i "$ASSET" -o "$SOURCE_PNG"

if [[ ! -s "$SOURCE_PNG" ]]; then
  echo "Failed to decode macOS app icon source image." >&2
  exit 1
fi

make_icon() {
  local size="$1"
  local output="$2"
  /usr/bin/sips -z "$size" "$size" "$SOURCE_PNG" --out "$ICONSET/$output" >/dev/null
}

make_icon 16   icon_16x16.png
make_icon 32   icon_16x16@2x.png
make_icon 32   icon_32x32.png
make_icon 64   icon_32x32@2x.png
make_icon 128  icon_128x128.png
make_icon 256  icon_128x128@2x.png
make_icon 256  icon_256x256.png
make_icon 512  icon_256x256@2x.png
make_icon 512  icon_512x512.png
make_icon 1024 icon_512x512@2x.png

/usr/bin/iconutil -c icns "$ICONSET" -o "$ICNS"

if [[ ! -s "$ICNS" ]]; then
  echo "Failed to create macOS .icns application icon." >&2
  exit 1
fi

echo "$ICNS"
