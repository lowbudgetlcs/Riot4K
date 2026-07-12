#!/usr/bin/env bash
# Starts/stops the mock Riot API server for out-of-process test suites.
#
#   samples/scripts/mock-server.sh start   # prints MOCK_RIOT_PORT=<port>
#   samples/scripts/mock-server.sh stop
#
# The port is also written to samples/build/mock-server.port, and appended to
# $GITHUB_ENV when running in GitHub Actions.
set -euo pipefail

SAMPLES_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
INSTALL_BIN="$SAMPLES_DIR/mock-riot-server/build/install/mock-riot-server/bin/mock-riot-server"
STATE_DIR="$SAMPLES_DIR/build"
PORT_FILE="$STATE_DIR/mock-server.port"
PID_FILE="$STATE_DIR/mock-server.pid"
LOG_FILE="$STATE_DIR/mock-server.log"

start() {
  if [[ ! -x "$INSTALL_BIN" ]]; then
    "$SAMPLES_DIR/../gradlew" -p "$SAMPLES_DIR" :mock-riot-server:installDist
  fi
  mkdir -p "$STATE_DIR"
  FIXTURES_DIR="$SAMPLES_DIR/fixtures" "$INSTALL_BIN" --port=0 > "$LOG_FILE" 2>&1 &
  echo $! > "$PID_FILE"

  for _ in $(seq 1 300); do
    # BSD-compatible extraction (macOS grep has no -P).
    port=$(sed -n 's/^MOCK_RIOT_SERVER_PORT=\([0-9][0-9]*\)$/\1/p' "$LOG_FILE" 2>/dev/null | head -n1)
    if [[ -n "${port:-}" ]]; then
      echo "$port" > "$PORT_FILE"
      echo "MOCK_RIOT_PORT=$port"
      if [[ -n "${GITHUB_ENV:-}" ]]; then
        echo "MOCK_RIOT_PORT=$port" >> "$GITHUB_ENV"
      fi
      return 0
    fi
    sleep 0.2
  done
  echo "mock server failed to start; log:" >&2
  cat "$LOG_FILE" >&2
  exit 1
}

stop() {
  if [[ -f "$PID_FILE" ]]; then
    kill "$(cat "$PID_FILE")" 2>/dev/null || true
    rm -f "$PID_FILE" "$PORT_FILE"
  fi
}

case "${1:-}" in
  start) start ;;
  stop) stop ;;
  *) echo "usage: $0 {start|stop}" >&2; exit 2 ;;
esac
