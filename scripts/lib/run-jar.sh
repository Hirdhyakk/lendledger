#!/usr/bin/env bash
# Usage: run-jar.sh <module-path> <artifact-id>
# Example: run-jar.sh services/auth-service auth-service
set -euo pipefail
MODULE_PATH="$1"
ARTIFACT_ID="$2"
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
JAR="$ROOT/$MODULE_PATH/target/${ARTIFACT_ID}-1.0.0-SNAPSHOT.jar"

if [[ ! -f "$JAR" ]]; then
  echo "Building $ARTIFACT_ID (run ./scripts/bootstrap.sh first for faster starts)..."
  mvn -f "$ROOT/pom.xml" -pl "$MODULE_PATH" -am package -DskipTests -q
fi

if [[ ! -f "$JAR" ]]; then
  echo "ERROR: JAR not found at $JAR"
  exit 1
fi

"$(dirname "$0")/wait-for-infra.sh"

echo "Starting $ARTIFACT_ID from $JAR"
echo "NOTE: This terminal is now dedicated to $ARTIFACT_ID."
echo "      Start each other service in a separate terminal (see QUICKSTART.sh)."
exec java ${JAVA_OPTS:-} -jar "$JAR"
