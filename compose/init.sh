#!/usr/bin/env bash
set -e -x
http POST localhost:8083/connectors/  < measures-connector.json
http GET localhost:8084/dispatch
http GET localhost:8080/init
