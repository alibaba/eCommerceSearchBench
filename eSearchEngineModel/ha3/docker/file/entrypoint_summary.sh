#!/usr/bin/env bash

hostname -i
cd /home/admin/

summary_name=summary-elasticsearch

mkdir ${summary_name}
tar -xzf elasticsearch-6.3.2.tar.gz
unzip elasticsearch-analysis-ik-6.3.2.zip -d elasticsearch-6.3.2/plugins/ik/

cp -r elasticsearch-6.3.2/* ${summary_name}/
cp ./summary_jvm.options ${summary_name}/config/jvm.options

rm -rf elasticsearch-6.3.2

machine=`uname -m`
if [[ $machine == "aarch64" ]];then
    echo "xpack.ml.enabled: false" >> ${summary_name}/config/elasticsearch.yml
fi

nohup ./${summary_name}/bin/elasticsearch -E http.port=9204 -E transport.tcp.port=9304 -E network.host=0.0.0.0 -d &
echo "${summary_name} started!"

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

