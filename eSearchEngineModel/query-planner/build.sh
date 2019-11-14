#!/usr/bin/env bash

# java build
mvn clean package
# copy file
if [[ ! -d "./docker/file" ]]; then
    mkdir ./docker/file
fi
cp ./target/query-planner-0.0.1-SNAPSHOT.jar ./docker/file/query-planner.jar
# rebuild image
cd ./docker/ && sh build.sh
