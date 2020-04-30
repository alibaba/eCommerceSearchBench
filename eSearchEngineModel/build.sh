#!/usr/bin/env bash

build(){
    pwdPath=`pwd`

    # build Base image
    cd ${pwdPath}
    cd base-image && sh build.sh
#     build Search Planner
    cd ${pwdPath}
    cd search-planner && sh build.sh
#     build Ranking Service
    cd ${pwdPath}
    cd ranking-service && sh build.sh
#    # build ha3
    cd ${pwdPath}
    cd ha3 && sh build.sh
    cd ${pwdPath}
    cd ha3 && sh build.sh searcher
    cd ${pwdPath}
    cd ha3 && sh build.sh summary
#     build Query Planner
    cd ${pwdPath}
    cd query-planner && sh build.sh
#     build tf-serving
    cd ${pwdPath}
    cd tf-serving && sh build.sh
    # build benchmark-cli
    cd ${pwdPath}
    cd ../data-generator && sh build.sh
    # build jmeter-image
    cd ${pwdPath}
    cd jmeter-image && sh build.sh

}

push()
{
    repo=hub.docker.com
    echo "## (you need to replace the repo name in build.sh then, ) input username and password of ${repo}"
    docker login ${repo}

    imgVer=latest
    baseImg=aliesearch-base-image
    spImg=aliesearch-search-planner
    rankingImg=aliesearch-ranking-service
    ha3Img=aliesearch-ha3
    qpImg=aliesearch-query-planner
    tfImg=aliesearch-tf-serving
    benchmarkCliImg=aliesearch-benchmark-cli
    jmeterImg=aliesearch-jmeter-image

    repoPath=${repo}/csp
    # base image
    baseImgRepo=${repoPath}/${baseImg}:${imgVer}
    docker rmi ${baseImgRepo}
    docker tag ${baseImg}:${imgVer} ${baseImgRepo}
    docker push ${baseImgRepo}
    # search planner
    spImgRepo=${repoPath}/${spImg}:${imgVer}
    docker rmi ${spImgRepo}
    docker tag ${spImg}:${imgVer} ${spImgRepo}
    docker push ${spImgRepo}
#     ranking service
    rankingImgRepo=${repoPath}/${rankingImg}:${imgVer}
    docker rmi ${rankingImgRepo}
    docker tag ${rankingImg}:${imgVer} ${rankingImgRepo}
    docker push ${rankingImgRepo}
#    # ha3
    ha3ImgRepo=${repoPath}/${ha3Img}:${imgVer}
    docker rmi ${ha3ImgRepo}
    docker tag ${ha3Img}:${imgVer} ${ha3ImgRepo}
    docker push ${ha3ImgRepo}
#     query planner
    qpImgRepo=${repoPath}/${qpImg}:${imgVer}
    docker rmi ${qpImgRepo}
    docker tag ${qpImg}:${imgVer} ${qpImgRepo}
    docker push ${qpImgRepo}
    # tf-serving
    tfImgRepo=${repoPath}/${tfImg}:${imgVer}
    docker rmi ${tfImgRepo}
    docker tag ${tfImg}:${imgVer} ${tfImgRepo}
    docker push ${tfImgRepo}
    # benchmark cli
    benchmarkCliImgRepo=${repoPath}/${benchmarkCliImg}:${imgVer}
    docker rmi ${benchmarkCliImgRepo}
    docker tag ${benchmarkCliImg}:${imgVer} ${benchmarkCliImgRepo}
    docker push ${benchmarkCliImgRepo}
    # jmeter
    jmeterImgRepo=${repoPath}/${jmeterImg}:${imgVer}
    docker rmi ${jmeterImgRepo}
    docker tag ${jmeterImg}:${imgVer} ${jmeterImgRepo}
    docker push ${jmeterImgRepo}
}
main(){
if [[ "build" != $1 && "push" != $1 ]]; then
    echo -e "usage:\n ./build build\n ./build push"
    exit;
fi

if [[ "build" == $1 || "build" == $2 ]]; then
    echo "building image..."
    build
fi

if [[ "push" == $1 || "push" == $2 ]]; then
    echo "pushing image..."
    push
fi
}

main "$@"
