#!/usr/bin/env bash

# java build
gradle clean build
# copy file
if [[ ! -d "./docker/file" ]]; then
    mkdir -p ./docker/file
fi
if [[ ! -d "./docker/file/data" ]]; then
    mkdir -p ./docker/file/data/
fi
cp ./build/distributions/benchmark-cli-1.0-SNAPSHOT.zip ./docker/file/benchmark-cli.zip
cp ./data/* ./docker/file/data/
# rebuild image
cd ./docker/ && sh build.sh
