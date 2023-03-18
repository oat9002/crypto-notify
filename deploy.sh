#!/usr/bin/bash

docker compose down
docker image rm oat9002/crypto-notify:latest
docker compose up -d
docker container ps
