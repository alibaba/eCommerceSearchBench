#!/usr/bin/env bash

hostname -i
cd /home/admin/

tar -zxf apache-jmeter-5.1.1.tar.gz

cp query_workload.csv apache-jmeter-5.1.1/
cp search_stress.jmx apache-jmeter-5.1.1/
cp search_stress_ha3.jmx apache-jmeter-5.1.1/

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

