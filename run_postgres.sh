#!/usr/bin/env bash

set -e

# Create the docker network if it does not exist
if ! docker network inspect banknet >/dev/null 2>&1; then
  echo "Creating docker network 'banknet'..."
  docker network create --driver bridge banknet
else
  echo "Docker network 'banknet' already exists."
fi

# Bring down and then up the compose environment
docker compose --file postgres-compose.yml down
docker compose --file postgres-compose.yml --compatibility up -d --build

# Tail the logs
docker logs --follow postgresql-master
