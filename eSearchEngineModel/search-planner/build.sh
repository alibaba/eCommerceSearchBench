#!/usr/bin/env bash

# java build
gradle clean build
# copy file
if [[ ! -d "./docker/file" ]]; then
    mkdir ./docker/file
fi
cp ./build/libs/search-planner-1.0-SNAPSHOT.jar ./docker/file/search-planner.jar
# rebuild image
cd ./docker/ && sh build.sh
