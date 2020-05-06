echo "QueryPlanner,ha3Searcher,Ranking,Summary,totalTime" > response_time_brkdwn.csv
cat out.log |grep queryPlannerTime |awk -F '[=,]' '{print $2,$4,$6,$7}'| awk -v OFS=',' '{print $1,$2,$3,$5 >> "response_time_brkdwn.csv"}'

echo "excellentTime,goodTime,badTime" > ha3_brkdwn.csv
cat out.log |grep excellentTime |awk -F '[=,]' -v OFS=',' '{print $2,$4,$6 >> "ha3_brkdwn.csv"}'
sed -i 's/-1/0/g' ha3_brkdwn.csv
