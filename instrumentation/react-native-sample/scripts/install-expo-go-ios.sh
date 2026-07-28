#!/usr/bin/env bash
set -euo pipefail

# Install or upgrade Expo Go on the booted iOS Simulator for the current Expo SDK.
# After an SDK upgrade, an older Expo Go build will fail to load the project.

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
cd "${PROJECT_DIR}"

EXPO_GO_BUNDLE_ID="host.exp.Exponent"

get_sdk_version() {
  npx expo config --json | node -pe "JSON.parse(require('fs').readFileSync(0)).sdkVersion"
}

get_expo_go_release_info() {
  local sdk_version="$1"
  curl -sf "https://exp.host/--/api/v2/versions" | node -e "
    const j = JSON.parse(require('fs').readFileSync(0, 'utf8'));
    const v = j.sdkVersions['${sdk_version}'];
    if (!v || !v.iosClientUrl || !v.iosClientVersion) process.exit(1);
    process.stdout.write(JSON.stringify({
      version: v.iosClientVersion,
      url: v.iosClientUrl,
    }));
  "
}

get_installed_expo_go_version() {
  local container
  container="$(xcrun simctl get_app_container booted "${EXPO_GO_BUNDLE_ID}" 2>/dev/null || true)"
  if [ -z "${container}" ]; then
    return 0
  fi
  defaults read "${container}/Info" CFBundleShortVersionString 2>/dev/null || true
}

is_expo_go_installed() {
  xcrun simctl listapps booted 2>/dev/null | grep -qi "host.exp.Exponent"
}

install_expo_go() {
  local sdk_version release_json expected_version url filename app_path
  sdk_version="$(get_sdk_version)"
  release_json="$(get_expo_go_release_info "${sdk_version}")"
  expected_version="$(node -pe "JSON.parse(process.argv[1]).version" "${release_json}")"
  url="$(node -pe "JSON.parse(process.argv[1]).url" "${release_json}")"
  filename="$(basename "${url}" .tar.gz)"
  app_path="${HOME}/.expo/ios-simulator-app-cache/${filename}.app"

  if [ -d "${app_path}" ] && [ -f "${app_path}/Info.plist" ]; then
    echo "Using cached Expo Go ${expected_version} at ${app_path}"
  else
    echo "Downloading Expo Go ${expected_version} for SDK ${sdk_version}..."
    rm -rf "${app_path}"
    mkdir -p "${app_path}"
    curl -Lf "${url}" | tar -xz -C "${app_path}"
  fi

  echo "Installing Expo Go ${expected_version} on simulator..."
  xcrun simctl install booted "${app_path}"
}

SDK_VERSION="$(get_sdk_version)"
RELEASE_JSON="$(get_expo_go_release_info "${SDK_VERSION}")"
EXPECTED_VERSION="$(node -pe "JSON.parse(process.argv[1]).version" "${RELEASE_JSON}")"
INSTALLED_VERSION="$(get_installed_expo_go_version)"

if ! is_expo_go_installed; then
  echo "Expo Go is not installed on the booted simulator."
  install_expo_go
elif [ "${INSTALLED_VERSION}" != "${EXPECTED_VERSION}" ]; then
  echo "Expo Go ${INSTALLED_VERSION:-unknown} is installed; SDK ${SDK_VERSION} requires ${EXPECTED_VERSION}."
  echo "Removing outdated Expo Go..."
  xcrun simctl uninstall booted "${EXPO_GO_BUNDLE_ID}" 2>/dev/null || true
  install_expo_go
else
  echo "Expo Go ${EXPECTED_VERSION} is already installed for SDK ${SDK_VERSION}."
fi
