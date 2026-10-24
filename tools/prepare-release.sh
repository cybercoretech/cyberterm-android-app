#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${CYBERTERM_RELEASE_STORE_FILE:?source ~/Vaults/android/cyberterm-release.env first}"
: "${CYBERTERM_RELEASE_STORE_PASSWORD:?release signing environment is incomplete}"
: "${CYBERTERM_RELEASE_KEY_ALIAS:?release signing environment is incomplete}"
: "${CYBERTERM_RELEASE_KEY_PASSWORD:?release signing environment is incomplete}"

cd "$ROOT"
ANDROID_NDK_HOME="${ANDROID_NDK_HOME:-$ANDROID_HOME/ndk/30.0.16248370}" ./gradlew assembleRelease
apk="app/build/outputs/apk/release/app-release.apk"
badging=$("$ANDROID_HOME/build-tools/36.0.0/aapt2" dump badging "$apk")
[[ "$badging" == *"name='dev.cybercore.cyberterm'"* ]] || { echo "wrong application id" >&2; exit 1; }
[[ "$badging" == *"versionCode='100'"* && "$badging" == *"versionName='0.1.0'"* ]] || { echo "unexpected release version" >&2; exit 1; }
permissions=$("$ANDROID_HOME/build-tools/36.0.0/aapt2" dump permissions "$apk")
[[ "$permissions" != *"uses-permission"* ]] || { echo "unexpected Android permissions" >&2; exit 1; }
"$ANDROID_HOME/build-tools/36.0.0/apksigner" verify --verbose "$apk"
mkdir -p dist
cp "$apk" dist/cyberterm.apk
(cd dist && sha256sum cyberterm.apk > cyberterm.apk.sha256)
echo "Release asset is ready: dist/cyberterm.apk"
