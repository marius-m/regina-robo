#!/usr/bin/env bash

java -Xmx1024m -Dspring.profiles.active=prod -Dserver.port=8082 -DLOG_PATH=./logs -DtoolPath=./robo-docker-run -DoutPath=./tts_output -DdockerHost=localhost -DdockerPort=8082 -jar ./ws/build/libs/ws-1.4.5.jar
