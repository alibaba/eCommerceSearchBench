#!/usr/bin/env bash
set -e
ver="latest"
name=aliesearch-base-image:${ver}
if [[ "$(docker images -q ${name} 2>/dev/null)" != "" ]]; then
    docker image rm ${name}
fi
docker build -f Dockerfile -t ${name} .
