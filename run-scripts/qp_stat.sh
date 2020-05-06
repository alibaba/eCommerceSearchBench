echo "UserDatabaseAccess,CategoryPredict,TensorFlowServing,totalTime" > qp_lat_brkdwn.csv
cat out.log |grep analyseQueryTime |awk -F '[=,]' -v OFS=',' '{print $2,$4,$6,$8 >> "qp_lat_brkdwn.csv"}'
