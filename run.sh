#!/usr/bin/env bash

set -e

docker ps -a
sleep 1

mvn package -T 1C -Dmaven.test.skip=true

docker compose up -d --build

docker ps -a
docker logs --follow sender
