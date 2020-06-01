#!/usr/bin/env bash

ver="latest"
name=aliesearch-benchmark-cli:${ver}
if [[ "$(docker images -q ${name} 2> /dev/null)" != "" ]]; then
    docker image rm ${name}
fi

if [[ ! -d "./file/data" ]]; then
    mkdir -p ./file/data/
fi

cp ../build/distributions/benchmark-cli-1.0-SNAPSHOT.zip ./file/benchmark-cli.zip
cp ../data/* ./file/data/
cd ./file/data/
ls |grep tar.gz |xargs -n1 tar zxvf

cd -
docker build -f Dockerfile -t ${name} .
