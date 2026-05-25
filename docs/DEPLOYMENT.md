# LendLedger — Deployment Guide

Deploy the full stack using free-tier friendly hosts:

| Piece | Platform |
|-------|----------|
| Frontend | **Vercel** |
| 5 Java services | **Render** (Docker) |
| PostgreSQL | **Neon** |
| Redis | **Upstash** |

Estimated time: **45–90 minutes** (mostly waiting for Render builds).

---

## Prerequisites

1. **GitHub account** and a repo containing this `lendledger/` project (push the folder as repo root or monorepo subfolder).
2. Accounts (all have free tiers): [Neon](https://neon.tech), [Upstash](https://upstash.com), [Render](https://render.com), [Vercel](https://vercel.com).

Generate secrets locally (save them in a password manager):

```bash
openssl rand -base64 48   # JWT_SECRET
openssl rand -base64 24   # INTERNAL_API_KEY
```

---

## Step 1 — Push code to GitHub

From your machine:

```bash
cd lendledger
git init
git add .
git commit -m "Initial LendLedger"
gh repo create lendledger --public --source=. --push
```

(Or create the repo on GitHub and `git remote add origin` + `git push`.)

---

## Step 2 — Neon (PostgreSQL)

1. Create a project at https://console.neon.tech
2. Copy connection details: **host**, **database name**, **user**, **password**
3. Open **SQL Editor** and run the contents of [`deploy/neon-init.sql`](../deploy/neon-init.sql):

```sql
CREATE SCHEMA IF NOT EXISTS auth;
CREATE SCHEMA IF NOT EXISTS loan;
CREATE SCHEMA IF NOT EXISTS payment;
CREATE SCHEMA IF NOT EXISTS notification;
```

Flyway will create tables on first service start (`SPRING_PROFILES_ACTIVE=prod` enables SSL to Neon).

---

## Step 3 — Upstash (Redis)

1. Create a Redis database at https://console.upstash.com
2. Copy the **TLS** URL (starts with `rediss://`) — use this as `REDIS_URL` on Render.

---

## Step 4 — Render (5 backend services)

### Option A — Blueprint (recommended)

1. Render Dashboard → **New** → **Blueprint**
2. Connect your GitHub repo
3. Render detects `render.yaml` at the repo root
4. Apply the blueprint (creates 5 web services + env group `lendledger-secrets`)
5. In **Environment Groups** → `lendledger-secrets`, set:

| Variable | Value |
|----------|--------|
| `DB_HOST` | Neon host |
| `DB_NAME` | Neon database |
| `DB_USER` | Neon user |
| `DB_PASSWORD` | Neon password |
| `REDIS_URL` | Upstash `rediss://...` |
| `JWT_SECRET` | Your generated secret |
| `INTERNAL_API_KEY` | Your generated secret |

6. Deploy **auth → loan → payment → notification → gateway** (or all at once; gateway must be last).

7. After each service has a public URL, set **service-specific** env vars:

**lendledger-loan**

```
AUTH_SERVICE_URL=https://lendledger-auth.onrender.com
PAYMENT_SERVICE_URL=https://lendledger-payment.onrender.com
```

**lendledger-payment**

```
LOAN_SERVICE_URL=https://lendledger-loan.onrender.com
```

**lendledger-gateway**

```
AUTH_SERVICE_URL=https://lendledger-auth.onrender.com
LOAN_SERVICE_URL=https://lendledger-loan.onrender.com
PAYMENT_SERVICE_URL=https://lendledger-payment.onrender.com
NOTIFICATION_SERVICE_URL=https://lendledger-notification.onrender.com
CORS_ORIGIN=https://YOUR-APP.vercel.app
```

Use your real Render hostnames (shown on each service’s page).

### Option B — Manual web services

For each service, **New → Web Service → Docker**, repo root, docker context `.`, and dockerfile:

| Service name | Dockerfile |
|--------------|------------|
| lendledger-auth | `deploy/docker/auth.Dockerfile` |
| lendledger-loan | `deploy/docker/loan.Dockerfile` |
| lendledger-payment | `deploy/docker/payment.Dockerfile` |
| lendledger-notification | `deploy/docker/notification.Dockerfile` |
| lendledger-gateway | `deploy/docker/gateway.Dockerfile` |

Copy env vars from [`deploy/env.example`](../deploy/env.example).

### Render tips

- **Free tier**: services sleep after ~15 min idle; first request may take 30–60s.
- **Health check**: `/actuator/health` (configured in `render.yaml`).
- **Logs**: if a service fails, check Flyway/DB SSL/Redis URL in logs.
- **Demo users**: auth-service seeds `admin@lendledger.local` / `password` on first boot.

---

## Step 5 — Vercel (frontend)

1. https://vercel.com/new → Import your GitHub repo
2. **Root Directory**: `frontend` (if repo root is `lendledger`, set root to `lendledger/frontend`)
3. Framework: Vite (auto-detected)
4. **Environment variable** (Production):

```
VITE_API_URL=https://lendledger-gateway.onrender.com/api
```

Replace with your gateway URL (must end with `/api`).

5. Deploy → note your URL (e.g. `https://lendledger-xxx.vercel.app`)

6. Go back to Render **gateway** → set `CORS_ORIGIN` to that Vercel URL (no trailing slash) → **Manual Deploy**

---

## Step 6 — Verify production

Wake services (open gateway health in browser), then:

```bash
GATEWAY_URL=https://lendledger-gateway.onrender.com ./scripts/smoke.sh
```

Or full API E2E:

```bash
API=https://lendledger-gateway.onrender.com/api ./scripts/e2e-api-test.sh
```

Open your Vercel URL → login `admin@lendledger.local` / `password`.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| Gateway 502 | Backend not running or wrong `*_SERVICE_URL` |
| CORS error in browser | `CORS_ORIGIN` on gateway must match Vercel URL exactly |
| DB connection failed | Check Neon host/user/password; schemas created? |
| Redis errors | Use Upstash `rediss://` URL in `REDIS_URL` |
| Flyway / schema error | Run `deploy/neon-init.sql` in Neon |
| Login 429 | Upstash rate limit; flush Redis or wait |
| Vercel API 404 | `VITE_API_URL` must include `/api` suffix |

---

## Security (before real users)

- Change demo passwords or disable `DataSeeder` for production.
- Use strong `JWT_SECRET` and `INTERNAL_API_KEY`.
- Prefer Render **paid** instances for always-on APIs.

---

## Reference

- Env template: [`deploy/env.example`](../deploy/env.example)
- Docker images: [`deploy/docker/`](../deploy/docker/)
- Blueprint: [`render.yaml`](../render.yaml)
