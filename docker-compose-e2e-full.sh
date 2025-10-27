#!/bin/bash
set -euo pipefail

# -------------------- окружение --------------------
source ./docker.properties

export COMPOSE_PROFILES=test
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"

export ALLURE_DOCKER_API=http://allure:5050/
export HEAD_COMMIT_MESSAGE="local build"
export ARCH=$(uname -m)

# Флаги управления пересборкой
FORCE_ALL=${FORCE_ALL:-0}      # 1 — пересобрать все сервисы
FORCE_FRONT=${FORCE_FRONT:-0}  # 1 — пересобрать фронт

# -------------------- хелперы ----------------------
image_exists() { docker image inspect "$1" >/dev/null 2>&1; }

build_module() {
  local gradle_task="$1"
  echo "### Build: $gradle_task ###"
  bash ./gradlew "$gradle_task" -x :rangiffler-e-2-e-tests:test
}

ensure_image() {
  local image="$1" gradle_task="$2"
  if [[ "$FORCE_ALL" == "1" ]]; then
    echo "↻ FORCE_ALL=1 → rebuild $image"
    build_module "$gradle_task"
  elif image_exists "$image"; then
    echo "✔ Image exists: $image — skip"
  else
    build_module "$gradle_task"
  fi
}

# ---------------- остановка текущего стека ----------
echo "### docker compose down (remove orphans) ###"
docker compose down --remove-orphans
./gradlew --stop || true

# Полный clean — только если явно попросили
if [[ "$FORCE_ALL" == "1" ]]; then
  echo "### Gradle clean (FORCE_ALL) ###"
  bash ./gradlew clean
fi

# ---------------- сервисные образы -----------------
ensure_image "${PREFIX}/rangiffler-auth-docker:latest"      ":rangiffler-auth:jibDockerBuild"
ensure_image "${PREFIX}/rangiffler-gateway-docker:latest"   ":rangiffler-gateway:jibDockerBuild"
ensure_image "${PREFIX}/rangiffler-geo-docker:latest"       ":rangiffler-geo:jibDockerBuild"
ensure_image "${PREFIX}/rangiffler-photo-docker:latest"     ":rangiffler-photo:jibDockerBuild"
ensure_image "${PREFIX}/rangiffler-userdata-docker:latest"  ":rangiffler-userdata:jibDockerBuild"
ensure_image "${PREFIX}/rangiffler-event-log-docker:latest" ":rangiffler-event-log:jibDockerBuild"

# ---------------- фронт ----------------------------
if [[ "$FORCE_FRONT" == "1" || ! $(docker image inspect "${PREFIX}/rangiffler-gql-client-docker:latest" >/dev/null 2>&1; echo $?) -eq 0 ]]; then
  echo "### Build frontend image (requested or missing) ###"
  docker build \
    --build-arg NPM_COMMAND=build:docker \
    -t "${PREFIX}/rangiffler-gql-client-docker:latest" \
    ./rangiffler-gql-client
else
  echo "✔ Front image exists — skip (use FORCE_FRONT=1 для ребилда)"
fi

# ---------------- e2e-тесты (всегда) ---------------
echo "### Build e2e tests image ###"
docker build \
  -t "${PREFIX}/rangiffler-e-2-e-tests:latest" \
  -f ./rangiffler-e-2-e-tests/Dockerfile \
  .

# ---------------- запуск стека ----------------------
echo "### docker compose up -d ###"
docker compose up -d

echo "### docker ps -a ###"
docker ps -a
