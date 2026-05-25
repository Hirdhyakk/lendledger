#!/usr/bin/env bash
# Prints quick start help. To run everything: ./run.sh
set -euo pipefail
cd "$(dirname "$0")"
chmod +x run.sh scripts/*.sh scripts/lib/*.sh 2>/dev/null || true

cat <<'EOF'
LendLedger — one command to run everything
==========================================

  1) Start Docker Desktop
  2) From this folder:

       ./run.sh

     Options:
       ./run.sh --skip-build      # skip Maven if already built
       ./run.sh --follow-logs     # stream logs; Ctrl+C stops all
       ./run.sh --help

  3) Open http://localhost:5173
     Login: admin@lendledger.local / password

  Stop all services:
       ./scripts/stop-all.sh

  Manual mode (separate terminals): see docs/UI_TESTING.md

EOF
