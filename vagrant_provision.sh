#!/usr/bin/env bash

sudo apt-get update
yes | sudo apt-get install wine-stable
sudo dpkg --add-architecture i386
sudo apt-get update
yes | sudo apt-get install wine32
yes | sudo apt-get install openjdk-8-jdk openjdk-8-jdk-headless
