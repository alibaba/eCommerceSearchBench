#!/usr/bin/env bash

hostname -i
env_check(){
  if [[ -z ${query_planner_host} || -z ${query_planner_port} ]]; then
    echo "query_planner_host and query_planner_port can't be empty!"
    exit -1
  fi
  if [[ -z ${excellent_item_host} || -z ${excellent_item_port} ]]; then
    echo "excellent_item_host and excellent_item_port can't be empty!"
    exit -1
  fi
  if [[ -z ${good_item_host} || -z ${good_item_port} ]]; then
    echo "good_item_host and good_item_port can't be empty!"
    exit -1
  fi
  if [[ -z ${bad_item_host} || -z ${bad_item_port} ]]; then
    echo "bad_item_host and bad_item_port can't be empty!"
    exit -1
  fi
  if [[ -z ${ranking_host} || -z ${ranking_port} ]]; then
    echo "ranking_host and ranking_port can't be empty!"
    exit -1
  fi
  if [[ -z ${summary_host} || -z ${summary_port} ]]; then
    echo "summary_host and summary_port can't be empty!"
    exit -1
  fi
}
env_check;

cd /home/admin/
ARGS="-server -Xms6g -Xmx6g -Xmn4g -XX:MetaspaceSize=256m \
    -XX:MaxMetaspaceSize=512m -XX:MaxDirectMemorySize=1g \
    -XX:SurvivorRatio=10 -XX:+UseConcMarkSweepGC \
    -XX:CMSMaxAbortablePrecleanTime=5000 -XX:+CMSClassUnloadingEnabled \
    -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly \
    -XX:+ExplicitGCInvokesConcurrent \
    -Dsun.rmi.dgc.server.gcInterval=2592000000 \
    -Dsun.rmi.dgc.client.gcInterval=2592000000 \
    -XX:ParallelGCThreads=6 -Xloggc:./gc.log \
    -XX:+PrintGCDetails -XX:+PrintGCDateStamps \
    -XX:+HeapDumpOnOutOfMemoryError \
    -Djava.awt.headless=true \
    -Dsun.net.client.defaultConnectTimeout=10000 \
    -Dsun.net.client.defaultReadTimeout=30000"
nohup java ${ARGS} -jar search-planner.jar >> out.log &
echo "search-planner started!"

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

