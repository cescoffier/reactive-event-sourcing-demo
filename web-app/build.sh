#!/usr/bin/env bash
set -x -e
mvn clean compile
docker build -t rxes/web-app .
