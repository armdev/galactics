#!/bin/bash

docker compose --file kafka-compose.yml --compatibility up -d --build
docker ps -a

docker logs --follow kafka-0

