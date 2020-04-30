#!/bin/sh
#****************************************************************#
# ScriptName: result.sh
# Author: $SHTERM_REAL_USER@alibaba-inc.com
# Create Date: 2020-04-01 17:07
# Modify Author: $SHTERM_REAL_USER@alibaba-inc.com
# Modify Date: 2020-04-01 17:07
# Function:
#***************************************************************#

echo "casename,sampleCount,mean_lat,min_lat,max_lat,90%_lat,95%_lat,99%_lat,qps" > lat_qps.csv
for file in `ls -rt |grep report`;do
{
    cat ${file}/statistics.json|tail -n 17 |awk -F '[:,]' -v pre=$file 'BEGIN{ret=pre} \
    {if($1 ~ "sampleCount") {ret=ret","""$2} \
    else if ($1 ~ "meanResTime") {ret=ret","""$2} \
    else if ($1 ~ "minResTime") {ret=ret","""$2} \
    else if ($1 ~ "maxResTime") {ret=ret","""$2} \
    else if ($1 ~ "pct1ResTime") {ret=ret","""$2} \
    else if ($1 ~ "pct2ResTime") {ret=ret","""$2} \
    else if ($1 ~ "pct3ResTime") {ret=ret","""$2} \
    else if ($1 ~ "throughput") {ret=ret","""$2} \
    } END {print ret >> "lat_qps.csv"}'
}
done

echo "casename,QueryPlanner,ha3Searcher,Ranking,Summary,totalTime" > response_time_brkdwn.csv
echo "casename,UserDatabaseAccess,CategoryPredict,TensorFlowServing,totalTime" > qp_latency_brkdwn.csv
echo "casename,excellentTime,goodTime,badTime" > ha3_latency_brkdwn.csv

for file in `ls -rt |grep _response_time_brkdwn.csv`;do
{
    cat $file |sed '1d' |awk -F ',' -v OFS=',' -v casename=${file%%_response*} 'BEGIN {QueryPlannersum=0;ha3Searchersum=0;Rankingsum=0;Summarysum=0;totalsum=0} {QueryPlannersum+=$1;ha3Searchersum+=$2;Rankingsum+=$3;Summarysum+=$4;totalsum+=$5} END{print casename,QueryPlannersum/NR,ha3Searchersum/NR,Rankingsum/NR,Summarysum/NR,(QueryPlannersum+ha3Searchersum+Rankingsum+Summarysum)/NR >> "response_time_brkdwn.csv"}'
}
done

for file in `ls -rt |grep qp_lat_brkdwn.csv`;do
{
    cat $file |sed '1d' |awk -F ',' -v OFS=',' -v casename=${file%%_qp*} 'BEGIN {UserDatabaseAccess=0;CategoryPredict=0;TensorFlowServing=0;totalTime=0} {UserDatabaseAccess+=$1;CategoryPredict+=$2;TensorFlowServing+=$3;totalTime+=$4} END{print casename,UserDatabaseAccess/NR,CategoryPredict/NR,TensorFlowServing/NR,totalTime/NR >> "qp_latency_brkdwn.csv"}'
}
done

for file in `ls -rt |grep ha3_brkdwn.csv`;do
{
    cat $file |sed '1d' |awk -F ',' -v OFS=',' -v casename=${file%%_ha3*} 'BEGIN {excellentTime=0;goodTime=0;badTime=0} {excellentTime+=$1;goodTime+=$2;badTime+=$3} END{print casename,excellentTime/NR,goodTime/NR,badTime/NR >> "ha3_latency_brkdwn.csv"}'
}
done

echo "pid,CpuUlti,Mem" > top_stat.csv && for file in `ls -rt |grep top`;do name=${file%%.*};sed "1d" $file | sed "s/^/${name}_/g" >> top_stat.csv;done
