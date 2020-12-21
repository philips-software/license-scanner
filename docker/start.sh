#!/bin/sh

# Make sure the script exits if a command returns non-zero exit code
set -e

findDockerExposedPortOnAWS() {
  RETRY_COUNTER=${RETRY_COUNTER:-3}
  COUNTER=0
  until [ -z `cat ${ECS_CONTAINER_METADATA_FILE} | jq -c -r ".ContainerID"` ] || [ $COUNTER -eq $RETRY_COUNTER ]; do
    echo "ECS metadata file not populated yet..."
    sleep 1
    COUNTER=$((COUNTER + 1))
  done
  export DOCKER_CONTAINER_ID=$(cat ${ECS_CONTAINER_METADATA_FILE} | jq -c -r ".ContainerID")
  export DOCKER_EXPOSED_PORT=$(cat ${ECS_CONTAINER_METADATA_FILE} | jq -c -r ".PortMappings[] | select(.ContainerPort == ${PORT:-8080}) | .HostPort")

  if [ -z ${DOCKER_EXPOSED_PORT} ]; then echo DOCKER_EXPOSED_PORT not set, port not found. && exit 1; fi
}

findDockerExposedPortViaDiscoveryAgent() {
  export DOCKER_CONTAINER_ID=$(cat /proc/self/cgroup  | grep "cpu:/" | sed 's/\([0-9]\):cpu:.*\///g')
  export NETWORK_BINDING=$(curl --retry 5 --connect-timeout 3 -X GET "http://${DOCKER_HOST_IP}:5555/container/${DOCKER_CONTAINER_ID}/portbinding?port=5000&protocol=tcp")
  export DOCKER_EXPOSED_PORT=$(echo ${NETWORK_BINDING} | jq -c '.[0].HostPort' | sed -e 's/^"//'  -e 's/"$//')
  if [ -z ${DOCKER_EXPOSED_PORT} ]; then echo DOCKER_EXPOSED_PORT not set, port not found. && exit 1; fi
}


if echo ${PROFILE} | grep -iqF aws; then
  echo AWS profile enabled

  export DOCKER_HOST_IP=$(curl --retry 5 --connect-timeout 3 -s 169.254.169.254/latest/meta-data/local-ipv4)

  if [ "$DISCOVERY_AGENT" ]; then
      findDockerExposedPortViaDiscoveryAgent
  else
      findDockerExposedPortOnAWS
  fi;

fi

if echo ${PROFILE} | grep -iqF docker-dev; then
  echo Docker dev profile enabled
  if [ -z ${DOCKER_HOST_IP} ]; then echo DOCKER_HOST_IP variable is not set. && exit 1; fi

  findDockerExposedPortViaDiscoveryAgent
fi

if echo ${PROFILE} | grep -iqF hipaa; then
  echo Docker hipaa-dev profile enabled
  source /app/retrieve_keystore.sh
fi


exec java -server ${JAVA_OPTS} -jar /app/service.jar
