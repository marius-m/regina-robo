#!/usr/bin/env bash
TARGET_PATH=./ws/build/libs
LATEST_JAR=$(ls -Art ${TARGET_PATH}/ws*.jar | tail -n 1)

echo "-- Running ${LATEST_JAR}"
java -Xmx1024m -Dspring.profiles.active=dev -Dserver.port=8082 -DLOG_PATH=./logs -DtoolPath=./robo-docker-run -DoutPath=./tts_output -DdockerHost=localhost -DdockerPort=8082 -jar $LATEST_JAR
