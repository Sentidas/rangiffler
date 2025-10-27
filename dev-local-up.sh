#!/usr/bin/env bash
# Только инфраструктура в Docker для локальной разработки
set -euo pipefail

PROJECT="rangiffler"
COMPOSE_FILE="docker-compose-local.yml"

need_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "Не найдена команда: $1"; exit 127; }; }
need_cmd docker

echo "==> Очистка контейнеров проекта (без удаления томов)"
docker compose -p "$PROJECT" -f "$COMPOSE_FILE" down --remove-orphans || true

echo "==> Старт инфраструктуры (MySQL, ZK/Kafka, Redis, MinIO, Jaeger)"
docker compose -p "$PROJECT" -f "$COMPOSE_FILE" up -d

echo "==> Проверка статуса контейнеров"
docker compose -p "$PROJECT" -f "$COMPOSE_FILE" ps

cat <<'EOF'

Готово. Инфраструктура поднята.

- MySQL:         localhost:3306 (root/root)
- ZooKeeper:     localhost:2181
- Kafka:         localhost:9092
- Redis:         localhost:6379
- MinIO API:     localhost:9002
- MinIO Console: http://localhost:9003 (root/rootroot)
- Jaeger UI:     http://localhost:16686

Сервисы и тесты запускаются отдельными командами (gradle/npm и т.п.).
EOF
