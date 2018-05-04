#!/usr/bin/env bash
set -e

eval $(minishift docker-env)

# Colors are important
export RED='\033[0;31m'
export NC='\033[0m' # No Color
export YELLOW='\033[0;33m'
export BLUE='\033[0;34m'

export OS_PROJECT_NAME="reactive-data-stream"

function warning {
    echo -e "  ${RED} $1 ${NC}"
}

function info {
    echo -e "  ${BLUE} $1 ${NC}"
}

oc version

if oc new-project "${OS_PROJECT_NAME}"; then
    info "Project ${OS_PROJECT_NAME} created"
else
    info "Reusing existing project ${OS_PROJECT_NAME}"
    oc project "${OS_PROJECT_NAME}"
fi

info "Enabling measures connector"
export DBZ_CONNECTOR="http://$(oc get route | grep debezium-connect | awk '{print $2}')"
http POST ${DBZ_CONNECTOR}/connectors/  < measures-connector.json

info "Starting dispatcher"
export DISPATCHER="http://$(oc get route | grep data-dispatcher | awk '{print $2}')"
http ${DISPATCHER}/dispatch

info "Starting web app"
export DISPATCHER="http://$(oc get route | grep web-app | awk '{print $2}')"
http ${DISPATCHER}/init

