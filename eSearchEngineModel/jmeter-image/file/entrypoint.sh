#!/usr/bin/env bash

hostname -i
cd /home/admin/

tar -zxf apache-jmeter-5.1.1.tar.gz

cp query_workload.csv apache-jmeter-5.1.1/
cp search_stress.jmx apache-jmeter-5.1.1/
cp search_stress_ha3.jmx apache-jmeter-5.1.1/

if [[ -n ${sp_ip} ]];then
    sed -i "s/172.17.0.6/${sp_ip}/" ./apache-jmeter-5.1.1/search_stress.jmx
fi

if [[ -n ${sp_port} ]];then
    sed -i "s/8080/${sp_port}/" ./apache-jmeter-5.1.1/search_stress.jmx
fi

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

