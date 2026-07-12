#!/usr/bin/env bash
# Local convenience for macOS: builds the XCFramework if missing, starts the
# mock server, runs swift test, and stops the server.
set -euo pipefail

SAMPLE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SAMPLE_DIR/../../.." && pwd)"
XCFRAMEWORK="$REPO_ROOT/riot4k-api/build/XCFrameworks/release/Riot4KSDK.xcframework"

if [[ ! -d "$XCFRAMEWORK" ]]; then
  "$REPO_ROOT/gradlew" :riot4k-api:assembleRiot4KSDKReleaseXCFramework
fi

"$REPO_ROOT/samples/scripts/mock-server.sh" start
trap '"$REPO_ROOT/samples/scripts/mock-server.sh" stop' EXIT

MOCK_RIOT_PORT="$(cat "$REPO_ROOT/samples/build/mock-server.port")" \
  swift test --package-path "$SAMPLE_DIR"
