#!/usr/bin/env bash
set -euo pipefail

# Install Expo Go on the booted iOS Simulator if missing (required for exp:// URLs).
# Error -10814 means no app is registered to handle the Expo URL scheme.

is_expo_go_installed() {
  xcrun simctl listapps booted 2>/dev/null | grep -qi "host.exp.Exponent"
}

get_expo_go_app_path() {
  local sdk_major sdk_minor sdk_version url filename app_path
  sdk_major="$(node -p "require('./package.json').dependencies.expo.match(/\\d+/)[0]")"
  sdk_minor="$(node -p "const m=require('./package.json').dependencies.expo.match(/~(\\d+)\\.(\\d+)/); m ? m[2] : '0'")"
  sdk_version="${sdk_major}.${sdk_minor}.0"

  url="$(curl -sf "https://exp.host/--/api/v2/versions" | node -e "
    const j=JSON.parse(require('fs').readFileSync(0,'utf8'));
    const v=j.sdkVersions['${sdk_version}'];
    if (!v || !v.iosClientUrl) process.exit(1);
    process.stdout.write(v.iosClientUrl);
  ")"

  filename="$(basename "${url}" .tar.gz)"
  app_path="${HOME}/.expo/ios-simulator-app-cache/${filename}.app"
  echo "${app_path}|${url}"
}

install_expo_go() {
  local app_path url info
  info="$(get_expo_go_app_path)"
  app_path="${info%%|*}"
  url="${info#*|}"

  if [ -d "${app_path}" ] && [ -f "${app_path}/Info.plist" ]; then
    echo "Using cached Expo Go at ${app_path}"
  else
    echo "Downloading Expo Go for iOS Simulator..."
    mkdir -p "${app_path}"
    curl -Lf "${url}" | tar -xz -C "${app_path}"
  fi

  echo "Installing Expo Go on simulator..."
  xcrun simctl install booted "${app_path}"
}

if ! is_expo_go_installed; then
  install_expo_go
fi
