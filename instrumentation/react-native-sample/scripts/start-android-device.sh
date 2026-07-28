#!/usr/bin/env bash
set -euo pipefail

if command -v lsof >/dev/null 2>&1; then
  lsof -ti :8081 | xargs kill -9 2>/dev/null || true
fi

# Physical Android devices need LAN or tunnel — not localhost.
exec npx expo start --android --clear --lan "$@"
