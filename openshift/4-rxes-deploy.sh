#!/usr/bin/env bash
#set -e

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

function fixPullPolicy {
    info "Patching $1"
    oc patch dc $1 -p "{\"spec\": {\"template\": {\"spec\": {\"containers\": [{\"name\": \"$1\", \"imagePullPolicy\":\"IfNotPresent\"}]}}}}"
}

function new-app {
    info "Deploying $1 (docker image: $2)"
    oc new-app --name=$1 rxes/$2
}
oc version

if oc new-project "${OS_PROJECT_NAME}"; then
    info "Project ${OS_PROJECT_NAME} created"
else
    info "Reusing existing project ${OS_PROJECT_NAME}"
    oc project "${OS_PROJECT_NAME}"
fi

new-app "data-generator" "data-generator-fluid"
fixPullPolicy "data-generator"
oc env dc/data-generator MYSQL_USER=my-user MYSQL_PASSWORD=password

new-app "data-dispatcher" "data-dispatcher-fluid"
fixPullPolicy "data-dispatcher"
oc expose service data-dispatcher

new-app "web-app" "web-app-fluid"
fixPullPolicy "web-app"
oc expose service web-app

new-app "alerting" "alerting-fluid"
fixPullPolicy "alerting"


