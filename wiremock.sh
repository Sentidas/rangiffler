#!/bin/bash

docker pull wiremock/wiremock:2.35.0
docker run --name rangiffler-mock \
  -p 8080:8080 \
  -v //c/Users/user/projects/2025/rangiffler/wiremock/rest/mappings:/home/wiremock/mappings \
  -d wiremock/wiremock:2.35.0 \
  --global-response-templating \
  --enable-stub-cors