#!/usr/bin/env bash
set -x

#source iplist_env.sh
machine=`uname -m`

img_ver=latest
tf_serving=aliesearch-tf-serving
query_planner=aliesearch-query-planner
ranking_service=aliesearch-ranking-service
ha3=aliesearch-ha3
ha3_searcher=aliesearch-ha3-searcher
ha3_summary=aliesearch-ha3-summary
search_planner=aliesearch-search-planner
benchmark_cli=aliesearch-benchmark-cli
jmeter_image=aliesearch-jmeter-image
# image url
repo=$2
if [ "x${repo}" != "x" ]; then
    img_tf_serving=${repo}/${tf_serving}:${img_ver}
    img_query_planner=${repo}/${query_planner}:${img_ver}
    img_ranking_service=${repo}/${ranking_service}:${img_ver}
    img_ha3=${repo}/${ha3}:${img_ver}
    img_ha3_searcher=${repo}/${ha3_searcher}:${img_ver}
    img_ha3_summary=${repo}/${ha3_summary}:${img_ver}
    img_search_planner=${repo}/${search_planner}:${img_ver}
    img_benchmark_cli=${repo}/${benchmark_cli}:${img_ver}
    img_jmeter_image=${repo}/${jmeter_image}:${img_ver}
else
    img_tf_serving=${tf_serving}:${img_ver}
    img_query_planner=${query_planner}:${img_ver}
    img_ranking_service=${ranking_service}:${img_ver}
    img_ha3=${ha3}:${img_ver}
    img_ha3_searcher=${ha3_searcher}:${img_ver}
    img_ha3_summary=${ha3_summary}:${img_ver}
    img_search_planner=${search_planner}:${img_ver}
    img_benchmark_cli=${benchmark_cli}:${img_ver}
    img_jmeter_image=${jmeter_image}:${img_ver}
fi

images=(${img_tf_serving}
${img_query_planner}
${img_ranking_service}
${img_ha3}
${img_search_planner}
${img_benchmark_cli}
${img_jmeter_image}
)

pull_all_images(){
  #sudo docker image rm ${img_tf_serving}
  #sudo docker image rm ${img_query_planner}
  #sudo docker image rm ${img_ranking_service}
  #sudo docker image rm ${img_ha3}
  #sudo docker image rm ${img_search_planner}
  #sudo docker image rm ${img_benchmark_cli}
  #sudo docker image rm ${img_jmeter_image}

  #sudo docker pull ${img_tf_serving}
  #sudo docker pull ${img_query_planner}
  #sudo docker pull ${img_ranking_service}
  #sudo docker pull ${img_ha3}
  #sudo docker pull ${img_search_planner}
  #sudo docker pull ${img_benchmark_cli}
  #sudo docker pull ${img_jmeter_image}
  for imageName in ${images[@]} ; do
      echo "${imageName}"
      sudo docker pull ${imageName}
  done
}
__container_ip=""
container_ip(){
  cid=`sudo docker ps -qf name=${machine}_${1}\$`
  if [[ -z ${cid} ]]; then
    echo "ERROR: container ${1} not start!!"
    exit -1
  fi
  __container_ip=`sudo docker inspect --format '{{.NetworkSettings.IPAddress}}' ${cid}`
  #__container_ip=`hostname -i`
  echo "${1} ip: ${__container_ip}"
}
stop_container(){
  cid=`sudo docker ps -qf name=${machine}_${1}\$`
  if [[ ! -z ${cid} ]]; then
    sudo docker rm -f ${cid}
    echo "stop container ${1}"
  else
    echo "container not exists: ${1}"
  fi
}
stop_all_containers(){
  echo "try to stop container: ${tf_serving}"
  stop_container ${tf_serving}
  echo "try to stop container: ${query_planner}"
  stop_container ${query_planner}
  echo "try to stop container: ${ranking_service}"
  stop_container ${ranking_service}
  echo "try to stop container: ${ha3}"
  #stop_container ${ha3}
  stop_container ${ha3_searcher}
  stop_container ${ha3_summary}
  echo "try to stop container: ${search_planner}"
  stop_container ${search_planner}
  echo "try to stop container: ${benchmark_cli}"
  stop_container ${benchmark_cli}
  echo "try to stop container: ${jmeter_image}"
  stop_container ${jmeter_image}
}
start_tf_serving(){
  echo "${tf_serving} starting..."
  nohup sudo docker run --rm --net=bridge -p 8501:8501 -m 16g \
    --sysctl net.ipv4.ip_local_port_range="1024 65000" \
    --name=${machine}_${tf_serving} ${img_tf_serving} &
  echo "${tf_serving} starting..."
}
start_query_planner(){
  echo "${query_planner} starting..."
  container_ip ${tf_serving}
  tf_serving_ip=${__container_ip}
  echo "tf_serving_ip=${__container_ip}" > iplist_env.sh

  nohup sudo docker run --rm --net=bridge -p 8088:8088  -p 7474:7474 -p 7687:7687 -m 16g  \
   -e tensor_flow_server_ip=${tf_serving_ip} \
   -e tensor_flow_server_port=8501 \
   --sysctl net.ipv4.ip_local_port_range="1024 65000" \
   --name=${machine}_${query_planner} ${img_query_planner} &
  echo "${query_planner} started"
}
start_ranking_service(){
  echo "${ranking_service} starting..."
  nohup sudo docker run --rm --net=bridge -m 16g -p 9203:9203 -p 9303:9303 \
   --sysctl net.ipv4.ip_local_port_range="1024 65000" \
   --name=${machine}_${ranking_service} ${img_ranking_service} &
  echo "${ranking_service} started"
}

start_ha3(){
  echo "${ha3} starting..."
  #nohup sudo docker run --rm --net=bridge -m 16g --ip ${ha3_ip} -p 9200:9200 -p 9204:9204 \
  # -p 9300:9300 -p 9304:9304 ---name=${machine}_${ha3} ${img_ha3} &
  nohup sudo docker run --rm --net=bridge -m 16g -p 9200:9200 -p 9300:9300 \
   --sysctl net.ipv4.ip_local_port_range="1024 65000" \
   --name=${machine}_${ha3_searcher} ${img_ha3_searcher} &
  nohup sudo docker run --rm --net=bridge -m 16g -p 9204:9204 -p 9304:9304 \
   --sysctl net.ipv4.ip_local_port_range="1024 65000" \
   --name=${machine}_${ha3_summary} ${img_ha3_summary} &
  echo "${ha3} started"
}
start_search_planner(){
  echo "${search_planner} starting..."
  container_ip ${query_planner}
  query_planner_ip=${__container_ip}
  echo "query_planner_ip=${__container_ip}" >> iplist_env.sh
  #container_ip ${ha3}
  #ha3_ip=${__container_ip}
  container_ip ${ha3_searcher}
  ha3_searcher_ip=${__container_ip}
  echo "ha3_searcher_ip=${__container_ip}" >> iplist_env.sh
  container_ip ${ha3_summary}
  ha3_summary_ip=${__container_ip}
  echo "ha3_summary_ip=${__container_ip}" >> iplist_env.sh
  container_ip ${ranking_service}
  ranking_service_ip=${__container_ip}
  echo "ranking_service_ip=${__container_ip}" >> iplist_env.sh

  nohup sudo docker run --rm --net=bridge -m 16g -p 8086:8080 \
    -e query_planner_host=${query_planner_ip} -e query_planner_port=8088 \
    -e excellent_item_host=${ha3_searcher_ip} -e excellent_item_port=9200 \
    -e good_item_host=${ha3_searcher_ip} -e good_item_port=9200 \
    -e bad_item_host=${ha3_searcher_ip} -e bad_item_port=9200 \
    -e ranking_host=${ranking_service_ip} -e ranking_port=9203 \
    -e summary_host=${ha3_summary_ip} -e summary_port=9204 \
    --sysctl net.ipv4.ip_local_port_range="1024 65000" \
    --name=${machine}_${search_planner} ${img_search_planner} &
  echo "${search_planner} started"
}
start_benchmark_cli(){
  echo "${benchmark_cli} starting..."
  container_ip ${query_planner}
  query_planner_ip=${__container_ip}
  #container_ip ${ha3}
  #ha3_ip=${__container_ip}
  container_ip ${ha3_searcher}
  ha3_searcher_ip=${__container_ip}
  container_ip ${ha3_summary}
  ha3_summary_ip=${__container_ip}
  container_ip ${ranking_service}
  ranking_service_ip=${__container_ip}

  nohup sudo docker run --rm --net=bridge -m 16g  \
    -e query_planner_host=${query_planner_ip} -e query_planner_port=8088 \
    -e excellent_item_host=${ha3_searcher_ip} -e excellent_item_port=9200 \
    -e good_item_host=${ha3_searcher_ip} -e good_item_port=9200 \
    -e bad_item_host=${ha3_searcher_ip} -e bad_item_port=9200 \
    -e ranking_host=${ranking_service_ip} -e ranking_port=9203 \
    -e summary_host=${ha3_summary_ip} -e summary_port=9204 \
    -e neo4j_host=${query_planner_ip} -e neo4j_port=7687 \
    --sysctl net.ipv4.ip_local_port_range="1024 65000" \
    --name=${machine}_${benchmark_cli} ${img_benchmark_cli} &
  echo "${benchmark_cli} started"
}
start_jmeter_image(){
  echo "${jmeter_image} starting..."
  container_ip ${search_planner}
  search_planner_ip=${__container_ip}
  echo "search_planner_ip=${__container_ip}" >> iplist_env.sh
  nohup sudo docker run --rm --net=bridge -m 8g \
      --sysctl net.ipv4.ip_local_port_range="1024 65000" \
      -e sp_ip=${search_planner_ip} -e sp_port=8080 --privileged -v `pwd`/jmeter_result:/home/admin/jmeter_result --name=${machine}_${jmeter_image} ${img_jmeter_image} &
  echo "${jmeter_image} started"
  container_ip ${benchmark_cli}
  echo "benchmark_cli_ip=${__container_ip}" >> iplist_env.sh
  container_ip ${ranking_service}
  echo "jmeter_ip=${__container_ip}" >> iplist_env.sh
}
start_all(){
  echo "# start_tf_serving..."
  start_tf_serving
  sleep 1
  echo "# start_query_planner..."
  start_query_planner
  sleep 1
  echo "# start_ha3..."
  start_ha3
  sleep 1
  echo "# start_ranking_service..."
  start_ranking_service
  sleep 1
  echo "# start_search_planner..."
  start_search_planner
  echo "wait for starting..."
  sleep 20
  echo "# start_benchmark_cli..."
  start_benchmark_cli
  sleep 1
  echo "# start_jmeter_image..."
  start_jmeter_image

  container_ip ${search_planner}
  search_planner_ip=${__container_ip}
  echo "###################################"
  echo "# all containers started, but data initializing may take several minutes, you can try:"
  echo "  curl -H 'Content-Type:application/json;charset=UTF-8' -d'
            {\"uid\":\"798\", \"page\":0, \"query\":\"68\"}' ${search_planner_ip}:8080/search"
  echo "# To check whether it is ready. If it returns right results, means service is ready"
  echo "# then login in jmeter container"
  echo "    sudo docker exec -it ${jmeter_image} bash"
  echo "# to start test"
}

main(){
  #if [[ $EUID -ne 0 ]]; then
  #  echo "This script must be run as root"
  #  exit 1
  #fi
  if [[ "start" == $1 ]]; then
    start_all
  elif [[ "stop" == $1 ]]; then
    echo "stop_all_containers..."
    stop_all_containers
  elif [[ "restart" == $1 ]]; then
    echo "stop_all_containers..."
    stop_all_containers
    start_all
  elif [[ "pull" == $1 ]]; then
    pull_all_images
  else
    echo -e "param must be one of:\npull \nstart \nstop \nrestart"
  fi
}
main "$@"

