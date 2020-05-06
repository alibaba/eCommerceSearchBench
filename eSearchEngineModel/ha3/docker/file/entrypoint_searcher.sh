#!/usr/bin/env bash

hostname -i
cd /home/admin/

excellent_name=excellent-elasticsearch

mkdir ${excellent_name}
tar -xzf elasticsearch-6.3.2.tar.gz
unzip elasticsearch-analysis-ik-6.3.2.zip -d elasticsearch-6.3.2/plugins/ik/

cp -r elasticsearch-6.3.2/* ${excellent_name}/
cp ./excellent_jvm.options ${excellent_name}/config/jvm.options

rm -rf elasticsearch-6.3.2

machine=`uname -m`
if [[ $machine == "aarch64" ]];then
    echo "xpack.ml.enabled: false" >> ${excellent_name}/config/elasticsearch.yml
fi

nohup ./${excellent_name}/bin/elasticsearch -E http.port=9200 -E transport.tcp.port=9300 -E network.host=0.0.0.0 -d &
echo "${excellent_name} started!"

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

