# CLI workflow — from zero to deployed

All commands run from the **`lendledger/`** folder unless noted.

## GitHub account (check / switch)

```bash
gh auth status
gh auth login          # first time
gh auth switch         # if you have multiple accounts
gh auth logout && gh auth login   # switch account completely
```

Your machine is using whatever account `gh auth status` shows (e.g. **Hirdhyakk**).

---

## 1. Git + GitHub (one-time)

Already done if you ran `setup-github.sh`. To repeat on another machine:

```bash
./scripts/setup-github.sh lendledger          # public repo
./scripts/setup-github.sh lendledger --private
```

Manual equivalent:

```bash
git init -b main
git add -A && git commit -m "Initial commit"
gh repo create lendledger --public --source=. --remote=origin --push
```

**Repo:** https://github.com/Hirdhyakk/lendledger

### Day-to-day git

```bash
git status
git add -A
git commit -m "Describe your change"
git push
```

Create a feature branch (optional):

```bash
git checkout -b feature/my-change
git push -u origin feature/my-change
gh pr create   # open a pull request
```

---

## 2. Local run

```bash
./run.sh
./scripts/stop-all.sh
./scripts/check-local.sh
./scripts/smoke-local.sh
```

---

## 3. Production secrets (generate once)

```bash
python3 -c "import secrets; print('JWT_SECRET=', secrets.token_urlsafe(48), sep='')"
python3 -c "import secrets; print('INTERNAL_API_KEY=', secrets.token_urlsafe(24), sep='')"
```

Copy into `deploy/env.example` locally (do not commit real values).

---

## 4. Neon (Postgres) — browser + optional CLI

**Console (easiest):**

1. https://console.neon.tech → create project
2. SQL Editor → paste `deploy/neon-init.sql`
3. Copy host, database, user, password

**Neon CLI (optional):**

```bash
brew install neonctl
neonctl auth
neonctl projects list
```

---

## 5. Upstash (Redis) — browser

1. https://console.upstash.com → create Redis database
2. Copy **TLS** URL (`rediss://...`) → `REDIS_URL`

---

## 6. Render (5 backends) — CLI

```bash
brew install render   # or: curl -fsSL https://render.com/install-cli | sh
render login
```

**Blueprint from repo (recommended):**

1. Dashboard → **New** → **Blueprint** → select `Hirdhyakk/lendledger`
2. Or after linking workspace: apply `render.yaml` from repo root

**Set secrets** (Dashboard → Environment Groups → `lendledger-secrets`, or CLI where supported):

- `DB_HOST`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- `REDIS_URL`, `JWT_SECRET`, `INTERNAL_API_KEY`

**After each service has a URL**, set inter-service URLs on loan, payment, gateway (see `deploy/env.example`).

**CLI helpers:**

```bash
render services list
render logs -r lendledger-gateway --tail
```

---

## 7. Vercel (frontend) — CLI

```bash
npm i -g vercel
cd frontend
vercel login
vercel link          # link to new project
```

Set production API URL:

```bash
vercel env add VITE_API_URL production
# paste: https://lendledger-gateway.onrender.com/api
vercel --prod
```

Or import repo in Vercel UI with root directory **`frontend`**.

Then update Render gateway `CORS_ORIGIN` to your Vercel URL and redeploy gateway.

---

## 8. Wake production APIs (run before each work session)

```bash
cd lendledger
./scripts/wake-prod.sh
```

## 9. Verify production

```bash
GATEWAY_URL=https://lendledger-gateway.onrender.com ./scripts/smoke.sh
```

---

## Quick reference

| Task | Command |
|------|---------|
| Who am I on GitHub? | `gh auth status` |
| Push code | `git push` |
| Open repo in browser | `gh repo view --web` |
| PR | `gh pr create` |
| Local stack | `./run.sh` |
| Deploy guide | [DEPLOYMENT.md](DEPLOYMENT.md) |
