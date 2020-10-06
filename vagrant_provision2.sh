#!/usr/bin/env bash

cd /home/vagrant
/usr/bin/java -DLOG_PATH=/home/vagrant/logs -DtoolPath=formatter -jar /home/vagrant/spring/ws-0.0.1.jar &
