#!/usr/bin/env bash
set -euo pipefail

# Free Metro port if a stale dev server is still running (common after SDK changes).
if command -v lsof >/dev/null 2>&1; then
  lsof -ti :8081 | xargs kill -9 2>/dev/null || true
fi

# Android emulator maps localhost to itself; forward Metro to the host machine.
if command -v adb >/dev/null 2>&1; then
  adb reverse tcp:8081 tcp:8081 2>/dev/null || true
fi

exec npx expo start --android --clear --localhost "$@"
