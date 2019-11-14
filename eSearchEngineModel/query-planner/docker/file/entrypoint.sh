#!/usr/bin/env bash


env_check(){
  if [[ -z ${tensor_flow_server_ip} || -z ${tensor_flow_server_port} ]]; then
    echo "tensor_flow_server_ip and tensor_flow_server_port can't be empty!"
    exit -1
  fi
}
env_check
hostname -i
cd /home/admin/

cat model_all.bin.* > model_all.bin
cat neo4j-community-3.5.6-unix.tar.gz.* > neo4j-community-3.5.6-unix.tar.gz

# start neo4j
tar -xzf neo4j-community-3.5.6-unix.tar.gz
cp neo4j.conf neo4j-community-3.5.6/conf/
./neo4j-community-3.5.6/bin/neo4j start

echo "sleep a while, wait neo4j start..."
sleep 20

export fasttext_model_path=/home/admin/model_all.bin

ARGS="-server -Xms4g -Xmx4g -Xmn2g -XX:MetaspaceSize=256m \
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
nohup java ${ARGS} -jar query-planner.jar >> out.log &
echo "search-planner started!"

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash
