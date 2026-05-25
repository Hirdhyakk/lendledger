.PHONY: bootstrap infra test smoke run stop help

help:
	@echo "Targets:"
	@echo "  make run        - start infra + all services + frontend (./run.sh)"
	@echo "  make stop       - stop all local services"
	@echo "  make bootstrap  - mvn clean install"
	@echo "  make infra      - docker compose up postgres + redis"
	@echo "  make test       - mvn clean test"
	@echo "  make smoke      - API smoke test (gateway must be running)"

run:
	./run.sh

stop:
	./scripts/stop-all.sh

bootstrap:
	mvn -B clean install -DskipTests

infra:
	./scripts/start-infra.sh

test:
	mvn -B clean test

smoke:
	./scripts/smoke-local.sh
