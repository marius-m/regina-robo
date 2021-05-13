#!/bin/bash

#set -x # Debug mode
DOCKER_NAME=$1
IN_VERSION_NAME=latest
BUILD_NAME="markmerkk/wine1"
ROOT_DIR=$(pwd)

echo "-- Running docker image --"
rm formatter-output/*
docker run --name ${DOCKER_NAME} --rm -v ${ROOT_DIR}/formatter-input:/tts_input:Z -v ${ROOT_DIR}/formatter-output:/tts_output:Z $BUILD_NAME:$IN_VERSION_NAME
