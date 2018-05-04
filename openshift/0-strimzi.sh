#!/usr/bin/env bash
set -e

eval $(minishift docker-env)

# Colors are important
export RED='\033[0;31m'
export NC='\033[0m' # No Color
export YELLOW='\033[0;33m'
export BLUE='\033[0;34m'

export OS_PROJECT_NAME="reactive-data-stream"
export STRIMZI_VERSION=0.3.0
export DEBEZIUM_VERSION=0.7.5

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

info "Deploying Kafka"
STRIMZI_DIR="strimzi-${STRIMZI_VERSION}"
if [ ! -d "${STRIMZI_DIR}" ] ; then
    echo -e "🔧 Retrieving Strimzi"
    git clone -b ${STRIMZI_VERSION} https://github.com/strimzi/strimzi "${STRIMZI_DIR}"
fi
cd "${STRIMZI_DIR}" || exit
oc login -u system:admin
oc create -f examples/install/cluster-controller && oc create -f examples/templates/cluster-controller

sleep 5

info "Instantiating Strimzi"
oc new-app strimzi-ephemeral -p CLUSTER_NAME=broker -p ZOOKEEPER_NODE_COUNT=1 -p KAFKA_NODE_COUNT=1 -p KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 -p KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1
oc new-app strimzi-connect-s2i -p CLUSTER_NAME=debezium -p KAFKA_CONNECT_BOOTSTRAP_SERVERS=broker-kafka:9092 -p KAFKA_CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR=1 -p KAFKA_CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR=1 -p KAFKA_CONNECT_STATUS_STORAGE_REPLICATION_FACTOR=1

sleep 10

info "Configuring Debezium Connect"
mkdir -p plugins
cd plugins
for PLUGIN in {mongodb,mysql,postgres}; do
    curl http://central.maven.org/maven2/io/debezium/debezium-connector-$PLUGIN/$DEBEZIUM_VERSION/debezium-connector-$PLUGIN-$DEBEZIUM_VERSION-plugin.tar.gz | tar xz;
done

oc start-build debezium-connect --from-dir=. --follow

cd ../.. || exit

oc expose service debezium-connect

