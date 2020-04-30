#!/bin/bash
#set -x
machine=`uname -m`
sudo docker cp data_regen_load.sh ${machine}_aliesearch-benchmark-cli:/home/admin/
sudo docker cp ${machine}_aliesearch-jmeter-image:/home/admin/apache-jmeter-5.1.1/search_stress.jmx ./
sudo docker cp sp_stat.sh ${machine}_aliesearch-search-planner:/home/admin
sudo docker cp qp_stat.sh ${machine}_aliesearch-query-planner:/home/admin
for c in ${machine}_aliesearch-search-planner ${machine}_aliesearch-query-planner ${machine}_aliesearch-ha3-summary ${machine}_aliesearch-ha3-searcher ${machine}_aliesearch-ranking-service;do
    sudo docker cp start_stat.sh ${c}:/home/admin
    sudo docker cp PerfMonitor.py ${c}:/home/admin
    #sudo docker exec $c cat /proc/sys/net/ipv4/ip_local_port_range
done
sudo docker cp ${machine}_aliesearch-jmeter-image:/home/admin/apache-jmeter-5.1.1/search_stress.jmx ./

for scale in 1; do
    sudo docker exec ${machine}_aliesearch-benchmark-cli bash /home/admin/data_regen_load.sh $scale
    sleep 1
    ./check_benchmark_status.sh iplist_env.sh > ./jmeter_result/data${scale}_status

    for parallel_thread in 100 300; do
    {
        sed -i "/TargetLevel/c\        <stringProp name=\"TargetLevel\">${parallel_thread}</stringProp>" search_stress.jmx
        if [[ $parallel_thread -eq 1000 ]];then
            sed -i "/RampUp/c\        <stringProp name=\"RampUp\">1</stringProp>" search_stress.jmx
            sed -i "/Steps/c\        <stringProp name=\"Steps\">1</stringProp>" search_stress.jmx
        fi
        sed -i "/Hold/c\        <stringProp name=\"Hold\">180</stringProp>" search_stress.jmx
    
        sudo docker cp search_stress.jmx ${machine}_aliesearch-jmeter-image:/home/admin/apache-jmeter-5.1.1
	for c in ${machine}_aliesearch-search-planner ${machine}_aliesearch-query-planner ${machine}_aliesearch-ha3-summary ${machine}_aliesearch-ha3-searcher ${machine}_aliesearch-ranking-service;do
        sudo docker exec $c bash /home/admin/start_stat.sh java ${c}_top.csv &
	done

   	if [[ -f ./jmeter_result/data${scale}_client${parallel_thread}_result ]];then sudo \rm ./jmeter_result/data${scale}_client${parallel_thread}_result;fi
   	if [[ -d ./jmeter_result/data${scale}_client${parallel_thread}_report ]];then sudo \rm -rf ./jmeter_result/data${scale}_client${parallel_thread}_report;fi
  
        sudo docker exec ${machine}_aliesearch-jmeter-image /home/admin/apache-jmeter-5.1.1/bin/jmeter -n -t /home/admin/apache-jmeter-5.1.1/search_stress.jmx -l /home/admin/jmeter_result/data${scale}_client${parallel_thread}_result -e -o /home/admin/jmeter_result/data${scale}_client${parallel_thread}_report
        sleep 5
        sudo docker exec ${machine}_aliesearch-search-planner bash /home/admin/sp_stat.sh
        sudo docker exec ${machine}_aliesearch-query-planner bash /home/admin/qp_stat.sh
    
       for file in ha3_brkdwn.csv response_time_brkdwn.csv  ${machine}_aliesearch-search-planner_top.csv 
       do
        sudo docker cp ${machine}_aliesearch-search-planner:/home/admin/${file} ./jmeter_result/data${scale}_client${parallel_thread}_${file}
       done
    
       for file in qp_lat_brkdwn.csv ${machine}_aliesearch-query-planner_top.csv
       do
           sudo docker cp ${machine}_aliesearch-query-planner:/home/admin/${file}  ./jmeter_result/data${scale}_client${parallel_thread}_${file} 
       done
   
	for c in ${machine}_aliesearch-ha3-summary ${machine}_aliesearch-ha3-searcher ${machine}_aliesearch-ranking-service;do 
            sudo docker cp ${c}:/home/admin/${c}_top.csv  ./jmeter_result/data${scale}_client${parallel_thread}_${c}_top.csv
	done
    }
    sleep 30
    done
done
