#!/bin/bash

#set -x # Debug mode
DOCKER_NAME=$1
IN_VERSION_NAME=latest
BUILD_NAME="markmerkk/wine1"
ROOT_DIR=$(pwd)

echo "-- Stopping docker image '${DOCKER_NAME}' --"
docker stop ${DOCKER_NAME}
