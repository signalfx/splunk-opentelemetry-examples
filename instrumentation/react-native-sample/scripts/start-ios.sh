#!/usr/bin/env bash
set -euo pipefail

# Opens Astronomy Shop in iOS Simulator without AppleScript (macOS Automation permission).
# Ensures the correct Expo Go build is installed for the current SDK before opening exp:// URLs.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_DIR}"

# shellcheck source=expo-dev-env.sh
source "${SCRIPT_DIR}/expo-dev-env.sh"

METRO_HOST="${REACT_NATIVE_PACKAGER_HOSTNAME}"
METRO_PORT="8081"
METRO_URL="http://${METRO_HOST}:${METRO_PORT}"
EXPO_URL="exp://${METRO_HOST}:${METRO_PORT}"

if command -v lsof >/dev/null 2>&1; then
  lsof -ti :"${METRO_PORT}" | xargs kill -9 2>/dev/null || true
fi

open -a Simulator

BOOTED="$(xcrun simctl list devices booted 2>/dev/null | grep -c "Booted" || true)"
if [ "${BOOTED:-0}" -eq 0 ]; then
  DEVICE_UDID="$(xcrun simctl list devices available 2>/dev/null | grep "iPhone" | head -1 | grep -oE '[0-9A-F-]{36}' | head -1 || true)"
  if [ -n "${DEVICE_UDID:-}" ]; then
    xcrun simctl boot "${DEVICE_UDID}" 2>/dev/null || true
  fi
fi

bash "${SCRIPT_DIR}/install-expo-go-ios.sh"

npx expo start --clear --localhost &
EXPO_PID=$!

cleanup() {
  kill "${EXPO_PID}" 2>/dev/null || true
}
trap cleanup EXIT

echo "Waiting for Metro at ${METRO_URL}..."
for _ in $(seq 1 90); do
  if curl -sf "${METRO_URL}/status" >/dev/null 2>&1; then
    if curl -sf -H "expo-platform: ios" "${METRO_URL}" | grep -q 'launchAsset'; then
      break
    fi
  fi
  sleep 1
done

if ! curl -sf "${METRO_URL}/status" >/dev/null 2>&1; then
  echo "Metro did not start at ${METRO_URL} within 90 seconds." >&2
  exit 1
fi

echo "Opening Astronomy Shop in iOS Simulator..."
bash "${SCRIPT_DIR}/open-expo-go-url-ios.sh" "${EXPO_URL}"

echo "Metro running at ${METRO_URL} (Ctrl+C to stop)"
wait "${EXPO_PID}"
