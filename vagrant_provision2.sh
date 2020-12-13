#!/usr/bin/env bash

cd /home/vagrant
/usr/bin/java -Dserver.port=8082 -Dspring.profiles.active=dev -DLOG_PATH=/home/vagrant/logs -DtoolPath=formatter -jar /home/vagrant/spring/ws-1.1.4.jar &
