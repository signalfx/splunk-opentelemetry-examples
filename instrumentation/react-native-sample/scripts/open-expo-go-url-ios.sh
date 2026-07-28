#!/usr/bin/env bash
set -euo pipefail

# Open an Expo dev server URL in Expo Go without AppleScript.
# Registers the exp:// scheme handler on the simulator (required on recent iOS runtimes).

EXPO_GO_BUNDLE_ID="host.exp.Exponent"
EXPO_URL="${1:-exp://localhost:8081}"

BOOTED_UDID="$(xcrun simctl list devices booted 2>/dev/null | grep -oE '[0-9A-F-]{36}' | head -1 || true)"
if [ -z "${BOOTED_UDID}" ]; then
  echo "No booted iOS simulator found." >&2
  exit 1
fi

SCHEME="$(node -pe "new URL(process.argv[1]).protocol.replace(':','')" "${EXPO_URL}")"
PLIST="${HOME}/Library/Developer/CoreSimulator/Devices/${BOOTED_UDID}/data/Library/Preferences/com.apple.launchservices.schemeapproval.plist"
PLIST_KEY="com.apple.CoreSimulator.CoreSimulatorBridge-->${SCHEME}"

mkdir -p "$(dirname "${PLIST}")"
if [ ! -f "${PLIST}" ]; then
  /usr/libexec/PlistBuddy -c "Add :${PLIST_KEY} string ${EXPO_GO_BUNDLE_ID}" "${PLIST}"
else
  /usr/libexec/PlistBuddy -c "Delete :${PLIST_KEY}" "${PLIST}" 2>/dev/null || true
  /usr/libexec/PlistBuddy -c "Add :${PLIST_KEY} string ${EXPO_GO_BUNDLE_ID}" "${PLIST}"
fi

echo "Opening ${EXPO_URL} in Expo Go..."
xcrun simctl openurl booted "${EXPO_URL}"
