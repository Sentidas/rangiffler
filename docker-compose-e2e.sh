#!/bin/bash
source ./docker.properties
export COMPOSE_PROFILES=test
export PROFILE=docker
export PREFIX="${IMAGE_PREFIX}"

export ALLURE_DOCKER_API=http://allure:5050/
export HEAD_COMMIT_MESSAGE="local build"
export ARCH=$(uname -m)

docker compose down
docker_containers=$(docker ps -a -q)
docker_images=$(docker images --format '{{.Repository}}:{{.Tag}}' | grep 'rangiffler')

#   ./docker-compose-dev.sh
#   ./docker-compose-dev.sh web,grpc
#   ./docker-compose-dev.sh web, grpc
RAW_TAGS="${*:-}"
if [[ -n "$RAW_TAGS" ]]; then
  INCLUDE_TAGS="${RAW_TAGS//[[:space:]]/}"
  export INCLUDE_TAGS
  echo "INCLUDE_TAGS=${INCLUDE_TAGS}"
else
  unset INCLUDE_TAGS || true
  echo "INCLUDE_TAGS=<ALL docker-safe>"
fi

if [ ! -z "$docker_containers" ]; then
  echo "### Stop containers: $docker_containers ###"
  docker stop $docker_containers
  docker rm $docker_containers
fi

if [ ! -z "$docker_images" ]; then
  echo "### Remove images: $docker_images ###"
  docker rmi $docker_images
fi

echo '### Java version ###'
java --version
bash ./gradlew clean
bash ./gradlew jibDockerBuild -x :rangiffler-e-2-e-tests:test

docker pull selenoid/vnc_chrome:127.0
docker compose up -d
docker ps -a
