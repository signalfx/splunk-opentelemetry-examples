#!/usr/bin/env bash
set -euo pipefail

# Opens Astronomy Shop in iOS Simulator without AppleScript (macOS Automation permission).
# Installs Expo Go first if needed — required to handle exp:// URLs.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_DIR}"

if command -v lsof >/dev/null 2>&1; then
  lsof -ti :8081 | xargs kill -9 2>/dev/null || true
fi

open -a Simulator

BOOTED="$(xcrun simctl list devices booted 2>/dev/null | grep -c "Booted" || true)"
if [ "${BOOTED:-0}" -eq 0 ]; then
  DEVICE_UDID="$(xcrun simctl list devices available 2>/dev/null | grep "iPhone" | head -1 | grep -oE '[0-9A-F-]{36}' | head -1 || true)"
  if [ -n "${DEVICE_UDID:-}" ]; then
    xcrun simctl boot "${DEVICE_UDID}" 2>/dev/null || true
  fi
fi

# Expo Go must be installed before opening exp://127.0.0.1:8081
bash "${SCRIPT_DIR}/install-expo-go-ios.sh"

npx expo start --clear --localhost &
EXPO_PID=$!

cleanup() {
  kill "${EXPO_PID}" 2>/dev/null || true
}
trap cleanup EXIT

for _ in $(seq 1 60); do
  if curl -sf "http://127.0.0.1:8081/status" >/dev/null 2>&1; then
    break
  fi
  sleep 1
done

echo "Opening Astronomy Shop in iOS Simulator..."
xcrun simctl openurl booted "exp://127.0.0.1:8081"

echo "Metro running at http://127.0.0.1:8081 (Ctrl+C to stop)"
wait "${EXPO_PID}"
