#!/bin/bash
set -x

if [[ $# -ne 1 || "$1"x = "-h"x || "$1"x = "--help"x ]]; then
    echo "Usage: ./check_benchmark_status.sh iplist_env"
    exit 0
fi

iplist=$1
iplist=${iplist:-iplist_env.sh}
source $iplist

curl ${query_planner_ip}:8088

#curl -H 'Content-Type:application/json;charset=UTF-8' -d'
#             {"uid":"798", "page":0, "query":"68"}' ${search_planner_ip}:8080/search
echo ""
echo "ha3 forward index info"
#curl ${ha3_ip}:9200/_cat/indices?v
curl ${ha3_searcher_ip}:9200/_cat/indices?v
echo "ha3 inverted index info"
curl ${ranking_service_ip}:9203/_cat/indices?v
echo "ha3 summary index info"
#curl ${ha3_ip}:9204/_cat/indices?v
curl ${ha3_summary_ip}:9204/_cat/indices?v

curl ${ha3_searcher_ip}:9200/_cat/segments?v |grep -v monitoring |grep -v watcher |grep -v kibana
curl ${ranking_service_ip}:9203/_cat/segments?v
