# Local setup & troubleshooting

## What went wrong in your logs

### 1. `Could not find artifact lendledger-common`

Running `mvn -pl services/auth-service spring-boot:run` **without** building the parent project first.

**Fix:** always use `-am` (also-make) or install once:

```bash
./scripts/bootstrap.sh
# OR
mvn clean install -DskipTests
```

Then run services with:

```bash
./scripts/run-auth.sh   # includes -am automatically
```

### 2. Docker / Redis / Postgres failed

```
failed to connect to the docker API ... docker.sock
```

**Fix:** open **Docker Desktop** and wait until it says “Running”, then:

```bash
./scripts/start-infra.sh
```

Without Docker you need local Postgres + Redis (Homebrew) and manual schema init — see `start-infra.sh` error message.

### 3. Pasting README comments

Lines starting with `#` in the README are **comments**, not commands. Do not paste them into the terminal.

---

## Correct startup order

```bash
cd lendledger
chmod +x scripts/*.sh

# 1. Build all modules (once per clone / after pull)
./scripts/bootstrap.sh

# 2. Database + Redis (Docker must be running)
./scripts/start-infra.sh

# 3. Five terminals — auth → loan → payment → notification → gateway (last)
./scripts/run-auth.sh
./scripts/run-loan.sh
./scripts/run-payment.sh
./scripts/run-notification.sh
./scripts/run-gateway.sh

# 4. Frontend
./scripts/run-frontend.sh
```

Verify: `./scripts/smoke-local.sh`

UI walkthrough: [UI_TESTING.md](./UI_TESTING.md)
