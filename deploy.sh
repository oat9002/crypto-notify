#!/usr/bin/bash

docker compose down
docker image rm oat9002/crypto-notify:latest
docker compse up -d
docker container ps