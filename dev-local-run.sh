#!/usr/bin/env bash
set -euo pipefail

PROJECT="rangiffler"
COMPOSE_FILE="docker-compose-local.yml"

# ---- Парсинг аргументов ----
AUTO_STOP=0
TAGS_RAW=""

# если последний аргумент == --stop → включаем автоостановку
last="${!#}"
if [[ $# -gt 0 && "$last" == --stop ]]; then
  AUTO_STOP=1
  # убираем последний аргумент (--stop)
  set -- "${@:1:$(($#-1))}"
fi

# остальные аргументы считаем тегами для e2e
for arg in "$@"; do
  TAGS_RAW+="${arg} "
done
TAGS="${TAGS_RAW//[[:space:]]/}"; TAGS="${TAGS//,,/,}"; TAGS="${TAGS#,}"; TAGS="${TAGS%,}"

# ---- Утилиты ----
need_cmd() { command -v "$1" >/dev/null 2>&1 || { echo "Не найдена команда: $1"; exit 127; }; }
need_cmd curl; need_cmd docker; need_cmd bash

wait_http() {
  local url="$1" tries="${2:-240}" sleep_sec="${3:-2}"
  for ((i=1;i<=tries;i++)); do
    curl -fsS "$url" >/dev/null 2>&1 && return 0
    sleep "$sleep_sec"
  done
  return 1
}

open_url() {
  local url="$1"
  if command -v cmd.exe >/dev/null 2>&1; then cmd.exe /c start "$url" >/dev/null 2>&1 || true
  elif command -v xdg-open >/dev/null 2>&1; then xdg-open "$url" >/dev/null 2>&1 || true
  elif command -v open >/dev/null 2>&1; then open "$url" >/dev/null 2>&1 || true
  fi
}

# ---- Очистка перед запуском ----
echo "==> Предочистка контейнеров проекта (без удаления томов)"
docker compose -p "$PROJECT" -f "$COMPOSE_FILE" down --remove-orphans || true

# ---- Запуск / Остановка ----
SERVICES=(rangiffler-auth rangiffler-userdata rangiffler-geo rangiffler-photo rangiffler-event-log rangiffler-gateway)
FRONT_DIR="rangiffler-gql-client"

start_services() {
  echo "==> Старт всех сервисов (bootRun)"
  for module in "${SERVICES[@]}"; do
    echo "   → ${module}"
    ./gradlew ":${module}:bootRun" --no-daemon -x test --args="--spring.profiles.active=local" >"${module}/build/bootrun.log" 2>&1 &
  done
}

stop_services() {
  echo "==> Остановка всех сервисов (bootRun)"
  pkill -f "rangiffler-.*bootRun" || true
}

start_front() {
  echo "==> Старт фронта (npm run dev)"
  (cd "$FRONT_DIR" && npm run dev > build/dev.log 2>&1 &)
}

stop_front() {
  echo "==> Остановка фронта"
  pkill -f "vite" || pkill -f "npm run dev" || true
}

stop_all() {
  echo "==> Останавливаем фронт и сервисы"
  stop_front
  stop_services
  docker compose -p "$PROJECT" -f "$COMPOSE_FILE" down --remove-orphans || true
}

# ---- Инфраструктура ----
echo "==> Поднимаем Docker инфраструктуру"
docker compose -p "$PROJECT" -f "$COMPOSE_FILE" up -d --wait || docker compose -p "$PROJECT" -f "$COMPOSE_FILE" up -d

# ---- Gradle ----
echo "==> Останавливаем Gradle daemon (если был)"
./gradlew --stop || true

# ---- Запуск ----
start_services
echo "==> Ждём readiness gateway..."
if ! wait_http "http://127.0.0.1:8081/actuator/health" 240 2; then
  echo "Gateway не отвечает."
  tail -n 100 rangiffler-gateway/build/bootrun.log || true
  exit 1
fi

start_front

# ---- E2E-тесты ----
echo "==> Запуск e2e тестов"
if [[ -n "$TAGS" ]]; then
  ./gradlew :rangiffler-e-2-e-tests:testLocal --no-daemon -Dtest.env=local -DincludeTags="$TAGS"
else
  ./gradlew :rangiffler-e-2-e-tests:testLocal --no-daemon -Dtest.env=local
fi

# ---- Автостоп ----
if [[ "$AUTO_STOP" -eq 1 ]]; then
  echo "==> Флаг --stop активирован: останавливаем всё"
  stop_all
fi

echo "==> Готово"
