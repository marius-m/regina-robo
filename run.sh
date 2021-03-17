#!/bin/bash

#set -x # Debug mode
IN_VERSION_NAME=$1

if [[ $# -eq 0 ]] ; then
    echo "Missing arguments! Ex.: ./run.sh {version}"
    exit 1
fi

OS_TYPE=$(uname)
BUILD_NAME="markmerkk/robo-regina"

echo "-- Running docker image --"
docker run -v /Users/mariusmerkevicius/Projects/personal/regina-robo/tts_output:/usr/local/res -e DOCKER_HOST=host.docker.internal -p 8080:8080 -v $BUILD_NAME:$IN_VERSION_NAME
