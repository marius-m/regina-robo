#!/bin/bash

#set -x # Debug mode
IN_VERSION_NAME=latest
BUILD_NAME="markmerkk/wine1"
ROOT_DIR=$(pwd)

echo "-- Running docker image --"
#docker run -v /Users/mariusmerkevicius/Projects/personal/regina-robo/tts_output:/usr/local/res -e DOCKER_HOST=host.docker.internal -p 8080:8080 $BUILD_NAME:$IN_VERSION_NAME
docker run -v ${ROOT_DIR}/formatter-input:/tts_input -v ${ROOT_DIR}/formatter-output:/tts_output -p 8082:8080 $BUILD_NAME:$IN_VERSION_NAME
