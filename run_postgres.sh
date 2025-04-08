#!/usr/bin/env bash

set -e

# Stop any running containers
docker compose --file keycloak-compose.yml down

# Start the containers
docker compose --file keycloak-compose.yml --compatibility up -d --build

# Tail Keycloak logs
docker logs --follow keycloak
