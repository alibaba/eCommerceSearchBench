#!/usr/bin/env bash

hostname -i
cd /home/admin/

name=ranking-service-elasticsearch
tar -xzf elasticsearch-6.3.2.tar.gz
unzip elasticsearch-analysis-ik-6.3.2.zip -d elasticsearch-6.3.2/plugins/ik/

mv elasticsearch-6.3.2 ${name}
cp ./jvm.options ${name}/config/

nohup ./${name}/bin/elasticsearch -E http.port=9203 -E transport.tcp.port=9303 -E network.host=0.0.0.0 -d &
echo "ranking-service-elasticsearch started!"

# wait forever
echo "entrypoint wait forever...."
tail -f /dev/null
#/bin/bash

