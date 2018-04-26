#!/usr/bin/env bash
set -e -x

(
    cd data-generator
    ./build.sh
)

(
    cd data-dispatcher
    ./build.sh
)

(
    cd web-app
    ./build.sh
)

(
    cd alerting
    ./build.sh
)

echo "Done!"