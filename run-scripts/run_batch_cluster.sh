#!/bin/bash
#set -x

if [[ $# -ne 1 || "$1"x = "-h"x || "$1"x = "--help"x ]]; then
    echo "Usage: ./run_batch_ca.sh iplist_env"
    exit 0
fi

machine=`uname -m`
source $1

copy_to_remote_docker(){
local_file=$1
dst_ip=$2
dst_container_and_dir=$3
scp $local_file $USER@${dst_ip}:~/
ssh $USER@${dst_ip} "sudo docker cp ~/${local_file} ${dst_container_and_dir}"
}

copy_from_remote_docker(){
dst_ip=$1
dst_container_and_dir=$2
dst_file=$3
local_dir=$4
scp $local_file $USER@${dst_ip}:~/
ssh $USER@${dst_ip} "sudo docker cp ${dst_container_and_dir}/${dst_file} ~/"
scp $USER@${dst_ip}:~/${dst_file} ${local_dir}
}

#sudo docker cp data_regen_load.sh aliesearch-benchmark-cli2:/home/admin/
copy_to_remote_docker data_regen_load.sh ${benchmark_cli_ip} aliesearch-benchmark-cli:/home/admin/
sudo docker cp aliesearch-jmeter-image:/home/admin/apache-jmeter-5.1.1/search_stress.jmx ./
copy_to_remote_docker sp_stat.sh ${search_planner_ip} aliesearch-search-planner:/home/admin
copy_to_remote_docker qp_stat.sh ${query_planner_ip} aliesearch-query-planner:/home/admin
for c in aliesearch-search-planner aliesearch-query-planner aliesearch-ha3-summary aliesearch-ha3-searcher aliesearch-ranking-service;do
    dst_ip_tmp=`echo $c |sed 's/-/_/g'|cut -d '_' -f 2,3`_ip
    dst_ip=$(grep ${dst_ip_tmp} $1 |cut -d '=' -f 2)
    copy_to_remote_docker start_stat.sh ${dst_ip} ${c}:/home/admin
    copy_to_remote_docker PerfMonitor.py ${dst_ip} ${c}:/home/admin
    #sudo docker exec $c cat /proc/sys/net/ipv4/ip_local_port_range
done

for scale in 200 500 1000; do
    if [[ $scale -ne 200 ]];then
        ssh $USER@${benchmark_cli_ip} "sudo docker exec aliesearch-benchmark-cli bash /home/admin/data_regen_load.sh $scale"
    fi
    sleep 1
    curl -XPOST http://${ha3_searcher_ip}:9200/_forcemerge?max_num_segments=1
    curl -XPOST http://${ranking_service_ip}:9203/_forcemerge?max_num_segments=1
    for replica in 0 ;do
        if [[ $replica -eq 0 ]];then
        curl -H "Content-Type: application/json" -XPUT ${ha3_searcher_ip}:9200/excellent_items/_settings -d '
	{
        "index" : {
            "number_of_replicas" : 0    }
	}'
	fi
        if [[ $replica -eq 1 ]];then
        curl -H "Content-Type: application/json" -XPUT ${ha3_searcher_ip}:9200/excellent_items/_settings -d '
	{
        "index" : {
            "number_of_replicas" : 1    }
	}'
	fi
        if [[ $replica -eq 2 ]];then
        curl -H "Content-Type: application/json" -XPUT ${ha3_searcher_ip}:9200/excellent_items/_settings -d '
	{
        "index" : {
            "number_of_replicas" : 2    }
	}'
	fi
    	sleep 30
        ./check_benchmark_status.sh $1 > ./jmeter_result/data${scale}_replica${replica}_status

        for parallel_thread in 100 300 ; do
        {
            sed -i "/TargetLevel/c\        <stringProp name=\"TargetLevel\">${parallel_thread}</stringProp>" search_stress.jmx
            if [[ $parallel_thread -eq 1000 ]];then
                sed -i "/RampUp/c\        <stringProp name=\"RampUp\">1</stringProp>" search_stress.jmx
                sed -i "/Steps/c\        <stringProp name=\"Steps\">1</stringProp>" search_stress.jmx
            fi
            sed -i "/Hold/c\        <stringProp name=\"Hold\">180</stringProp>" search_stress.jmx
        
            sudo docker cp search_stress.jmx aliesearch-jmeter-image:/home/admin/apache-jmeter-5.1.1
            for c in aliesearch-search-planner aliesearch-query-planner aliesearch-ha3-summary aliesearch-ha3-searcher aliesearch-ranking-service;do
            dst_ip_tmp=`echo $c |sed 's/-/_/g'|cut -d '_' -f 2,3`_ip
            dst_ip=$(grep ${dst_ip_tmp} $1 |cut -d '=' -f 2)
            ssh $USER@${dst_ip} "sudo docker exec $c bash /home/admin/start_stat.sh java ${c}_top.csv &"
            done

            if [[ -f ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_result ]];then sudo \rm ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_result;fi
            if [[ -d ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_report ]];then sudo \rm -rf ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_report;fi
  
            sudo docker exec aliesearch-jmeter-image /home/admin/apache-jmeter-5.1.1/bin/jmeter -n -t /home/admin/apache-jmeter-5.1.1/search_stress.jmx -l /home/admin/jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_result -e -o /home/admin/jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_report
            sleep 5
            ssh $USER@${search_planner_ip} "sudo docker exec aliesearch-search-planner bash /home/admin/sp_stat.sh"
            ssh $USER@${query_planner_ip} "sudo docker exec aliesearch-query-planner bash /home/admin/qp_stat.sh"
        
           for file in ha3_brkdwn.csv response_time_brkdwn.csv  aliesearch-search-planner_top.csv 
           do
            #sudo docker cp aliesearch-search-planner:/home/admin/${file} ./jmeter_result/data${scale}_client${parallel_thread}_${file}
            copy_from_remote_docker ${search_planner_ip} aliesearch-search-planner:/home/admin ${file} ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_${file}
           done
        
           for file in qp_lat_brkdwn.csv aliesearch-query-planner_top.csv
           do
               copy_from_remote_docker ${query_planner_ip} aliesearch-query-planner:/home/admin ${file}  ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_${file} 
           done
   
            for c in aliesearch-ha3-summary aliesearch-ha3-searcher aliesearch-ranking-service;do 
            dst_ip_tmp=`echo $c |sed 's/-/_/g'|cut -d '_' -f 2,3`_ip
            dst_ip=$(grep ${dst_ip_tmp} $1 |cut -d '=' -f 2)
                copy_from_remote_docker ${dst_ip} ${c}:/home/admin ${c}_top.csv  ./jmeter_result/data${scale}_client${parallel_thread}_replica${replica}_${c}_top.csv
            done
        }
    done
    sleep 30
    done
done
