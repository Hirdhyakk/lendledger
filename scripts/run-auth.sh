#!/usr/bin/env bash
exec "$(dirname "$0")/lib/run-jar.sh" services/auth-service auth-service
