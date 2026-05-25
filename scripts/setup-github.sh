#!/usr/bin/env bash
# One-time: init git, commit, create GitHub repo, push main.
# Usage: ./scripts/setup-github.sh [repo-name] [--private]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

REPO_NAME="${1:-lendledger}"
VISIBILITY="--public"
if [[ "${2:-}" == "--private" ]]; then
  VISIBILITY="--private"
fi

if ! command -v gh >/dev/null 2>&1; then
  echo "Install GitHub CLI: brew install gh && gh auth login"
  exit 1
fi

echo "==> GitHub account:"
gh auth status

if [[ -d .git ]]; then
  echo "==> Git already initialized"
else
  git init -b main
fi

git add -A
if git diff --cached --quiet 2>/dev/null; then
  echo "==> Nothing to commit (working tree clean)"
else
  git commit -m "$(cat <<'EOF'
Initial LendLedger: microservices LMS + React UI

Spring Boot services, API gateway, Docker/Render deploy config, and local run scripts.
EOF
)"
fi

if git remote get-url origin >/dev/null 2>&1; then
  echo "==> Remote origin already set: $(git remote get-url origin)"
  git push -u origin main
else
  echo "==> Creating GitHub repo: $REPO_NAME ($VISIBILITY)"
  gh repo create "$REPO_NAME" $VISIBILITY --source=. --remote=origin --push
fi

echo ""
echo "Done. Repo: $(gh repo view --json url -q .url 2>/dev/null || echo "see: gh repo view")"
echo "Next: follow docs/DEPLOYMENT.md (Neon → Upstash → Render Blueprint → Vercel)"
