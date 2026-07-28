#!/usr/bin/env bash
# Metro with --localhost binds to the "localhost" hostname (often IPv6-only on macOS).
# Default manifest URLs use 127.0.0.1, which cannot reach that listener.
export REACT_NATIVE_PACKAGER_HOSTNAME="${REACT_NATIVE_PACKAGER_HOSTNAME:-localhost}"
