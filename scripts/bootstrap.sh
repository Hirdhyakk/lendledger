#!/usr/bin/env bash
# One-time / after-pull: build all modules so lendledger-common is in local Maven repo.
set -euo pipefail
cd "$(dirname "$0")/.."
echo "==> Building all modules (install to ~/.m2)..."
mvn -B clean install -DskipTests
echo "==> Done. Common module is installed; you can run services with scripts/run-*.sh"
