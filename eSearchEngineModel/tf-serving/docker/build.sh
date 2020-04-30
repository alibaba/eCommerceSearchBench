#!/usr/bin/env bash

ver="latest"
name=aliesearch-tf-serving:${ver}
if [[ "$(docker images -q ${name} 2> /dev/null)" != "" ]]; then
    docker rmi ${name}
fi

machine=`uname -m`
if [[ $machine == "x86_64" ]];then
    docker build -f Dockerfile -t ${name} .
elif [[ $machine == "aarch64" ]];then
    docker build -f Dockerfile_arm64v8 -t ${name} .
else
    echo "Unsupported machine platform!"
fi
