#!/usr/bin/env bash
# Start a Spring Boot JAR in the background; write PID and log under logs/
set -euo pipefail
MODULE_PATH="$1"
ARTIFACT_ID="$2"
PORT="${3:-}"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOG_DIR="$ROOT/logs"
JAR="$ROOT/$MODULE_PATH/target/${ARTIFACT_ID}-1.0.0-SNAPSHOT.jar"
PID_FILE="$LOG_DIR/${ARTIFACT_ID}.pid"
LOG_FILE="$LOG_DIR/${ARTIFACT_ID}.log"

mkdir -p "$LOG_DIR"

if [[ ! -f "$JAR" ]]; then
  echo "Building $ARTIFACT_ID..."
  mvn -f "$ROOT/pom.xml" -pl "$MODULE_PATH" -am package -DskipTests -q
fi

if [[ -f "$PID_FILE" ]]; then
  old_pid=$(cat "$PID_FILE")
  if kill -0 "$old_pid" 2>/dev/null; then
    echo "Stopping previous $ARTIFACT_ID (pid $old_pid)"
    kill "$old_pid" 2>/dev/null || true
    sleep 1
  fi
fi

if [[ -n "$PORT" ]] && lsof -ti ":$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Port $PORT in use; freeing for $ARTIFACT_ID..."
  lsof -ti ":$PORT" -sTCP:LISTEN | xargs kill 2>/dev/null || true
  sleep 1
fi

nohup java ${JAVA_OPTS:-} -jar "$JAR" >>"$LOG_FILE" 2>&1 &
echo $! >"$PID_FILE"
echo "$ARTIFACT_ID started (pid $(cat "$PID_FILE"), log logs/${ARTIFACT_ID}.log)"
