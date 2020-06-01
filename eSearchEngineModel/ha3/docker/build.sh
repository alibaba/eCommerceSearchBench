#!/usr/bin/env bash
ha3_type=$1
ver="latest"
if [[ "x${ha3_type}" == "x" ]];then
    name=aliesearch-ha3:${ver}
    cp ./file/entrypoint_ha3.sh ./file/entrypoint.sh
elif [[ "x${ha3_type}" == "xsearcher" ]];then
    name=aliesearch-ha3-${ha3_type}:${ver}
    cp ./file/entrypoint_searcher.sh ./file/entrypoint.sh
elif [[ "x${ha3_type}" == "xsummary" ]];then
    name=aliesearch-ha3-${ha3_type}:${ver}
    cp ./file/entrypoint_summary.sh ./file/entrypoint.sh
else
    echo "Unsupported ha3 type!"
fi

if [[ "$(docker images -q ${name} 2> /dev/null)" != "" ]]; then
    docker rmi ${name}
fi
docker build -f Dockerfile -t ${name} .
