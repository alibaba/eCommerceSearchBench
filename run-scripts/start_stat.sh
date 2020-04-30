#!/bin/bash
#set -x
echo 1 > out.log

echo "pid,CpuUlti,Mem" > $2

sleep 20

python PerfMonitor.py top -t process -p $1 -n 5 -i 1 -c test001 | grep Finished |awk -F '[,=]' -v OFS=',' -v csv="$2" '{print $2,$4,$6 >> csv}'

