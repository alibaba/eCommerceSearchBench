#!/usr/bin/env bash

hostname -i
cd /home/admin/

scale_factor=$1
scale_factor=${scale_factor:-10}

env_check(){
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
  if [[ -z ${neo4j_host} || -z ${neo4j_port} ]]; then
    echo "neo4j_host and neo4j_port can't be empty!"
    exit -1
  fi
}
env_check;
echo "index creating...."
sh create_index.sh
echo "index created!"

#unzip benchmark-cli.zip -d benchmark-cli_tmp && \
#  mv "./benchmark-cli_tmp/`ls benchmark-cli_tmp`" benchmark-cli && \
#  rm -r benchmark-cli_tmp
#cp /home/admin/*.txt /home/admin/benchmark-cli/

JAVA_OPTS=" -Xms4g -Xmx4g -Xmn2g -XX:SurvivorRatio=8 -XX:+UseConcMarkSweepGC -XX:CMSMaxAbortablePrecleanTime=5000 -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=80 -XX:+UseCMSInitiatingOccupancyOnly -XX:+ExplicitGCInvokesConcurrent -XX:ParallelGCThreads=6"
export JAVA_OPTS

echo "data generating with scale_factor = $scale_factor ..."
cd benchmark-cli && ./bin/benchmark-cli generate --scale $scale_factor | tee /home/admin/out.log
echo "data generated!"

echo "data loading..."
bin/benchmark-cli load \
 --excellent-item-host=${excellent_item_host} --excellent-item-port=${excellent_item_port:-9200} \
 --good-item-host=${good_item_host} --good-item-port=${good_item_port:-9201} \
 --bad-item-host=${bad_item_host} --bad-item-port=${bad_item_port:-9202} \
 --ranking-host=${ranking_host} --ranking-port=${ranking_port:-9203} \
 --summary-host=${summary_host} --summary-port=${summary_port:-9204} \
 --neo4j-uri=bolt://${neo4j_host}:${neo4j_port:-7687} | tee /home/admin/out.log
echo "data loaded!"

# wait forever
#echo "entrypoint wait forever...."
#tail -f /dev/null
#/bin/bash
