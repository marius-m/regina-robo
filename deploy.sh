#!/bin/bash

#set -x # Debug mode
IN_VERSION_NAME=$1

if [[ $# -eq 0 ]] ; then
    echo "Missing arguments! Ex.: ./deploy.sh {version}"
    exit 1
fi

OS_TYPE=$(uname)
BUILD_NAME="markmerkk/robo-regina"
TMP_DIR="tmp-docker1"
TMP_IMAGE=${TMP_DIR}/tmp.tar

echo "-- Storing docket image --"
mkdir ${TMP_DIR}
docker save -o ${TMP_IMAGE} ${BUILD_NAME}:$IN_VERSION_NAME

echo "-- Deploying docker image --"
scp ${TMP_IMAGE} ded_u3:~/rss/

echo "-- Clean up --"
rm -r ${TMP_DIR}
