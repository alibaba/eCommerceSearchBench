/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.benchmark.stat;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author CarpenterLee
 */
//@Component
public class StatService {
    static final Logger log = LoggerFactory.getLogger(StatService.class);

    private Stat totalStat = new Stat();
    private Stat qpStat = new Stat();
    private Stat ha3SearcherStat = new Stat();
    private Stat rankingStat = new Stat();
    private Stat ha3SummaryStat = new Stat();
    private boolean ha3SearcherOnly = false;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    private volatile long lastUpdate = -1;
    private volatile boolean dataUpdate = false;

    public static void main(String[] args) {
        StatService service = new StatService();
        service.accept(1, 1, 1, 1, 1);
        System.out.println(service.logString());
    }

    public StatService() {
        this(false);
    }

    public StatService(boolean ha3SearcherOnly) {
        this.ha3SearcherOnly = ha3SearcherOnly;
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            if (dataUpdate) {
                log.info("statistics: {}", this.logString());
            }
            // clear data 30s before
            if (System.currentTimeMillis() - lastUpdate > 30 * 1000) {
                clear();
            }
        }, 4, 10, TimeUnit.SECONDS);
    }

    public synchronized void accept(long totalCost,
                                    long qpCost,
                                    long ha3SearcherCost,
                                    long rankingCost,
                                    long ha3SummaryCost) {
        totalStat.accept(totalCost);
        qpStat.accept(qpCost);
        ha3SearcherStat.accept(ha3SearcherCost);
        rankingStat.accept(rankingCost);
        ha3SummaryStat.accept(ha3SearcherCost);
        lastUpdate = System.currentTimeMillis();
        dataUpdate = true;

    }

    public synchronized void acceptHa3Searcher(long totalCost, long cost) {
        totalStat.accept(totalCost);
        ha3SearcherStat.accept(cost);
        lastUpdate = System.currentTimeMillis();
        dataUpdate = true;
    }

    public synchronized void clear() {
        qpStat.clear();
        ha3SearcherStat.clear();
        ha3SummaryStat.clear();
        rankingStat.clear();
        dataUpdate = false;
    }

    public synchronized String logString() {
        StringBuilder builder = new StringBuilder("\n---------statistics----------\n");
        builder.append("total requests: " + totalStat.size() + "\n");
        builder.append("latency statistics(ms): \n");
        String template = "%-20s: min=%6d, max=%6d, avg=%9.2f, P90=%6d, P95=%6d, P99=%6d\n";
        double p90 = 0.9;
        double p95 = 0.95;
        double p99 = 0.99;
        builder.append(String.format(template, "TotalTimeCost",
            totalStat.min(),
            totalStat.max(),
            totalStat.avg(),
            totalStat.percentile(p90),
            totalStat.percentile(p95),
            totalStat.percentile(p99)));
        if (!ha3SearcherOnly) {
            builder.append(String.format(template, "SearchPlanner",
                qpStat.min(),
                qpStat.max(),
                qpStat.avg(),
                qpStat.percentile(p90),
                qpStat.percentile(p95),
                qpStat.percentile(p99)));
        }
        builder.append(String.format(template, "HA3Searcher",
            ha3SearcherStat.min(),
            ha3SearcherStat.max(),
            ha3SearcherStat.avg(),
            ha3SearcherStat.percentile(p90),
            ha3SearcherStat.percentile(p95),
            ha3SearcherStat.percentile(p99)));
        if (!ha3SearcherOnly) {
            builder.append(String.format(template, "Ranking",
                rankingStat.min(),
                rankingStat.max(),
                rankingStat.avg(),
                rankingStat.percentile(p90),
                rankingStat.percentile(p95),
                rankingStat.percentile(p99)));
        }
        if (!ha3SearcherOnly) {
            builder.append(String.format(template, "HA3Summary",
                ha3SummaryStat.min(),
                ha3SummaryStat.max(),
                ha3SummaryStat.avg(),
                ha3SummaryStat.percentile(p90),
                ha3SummaryStat.percentile(p95),
                ha3SummaryStat.percentile(p99)));
        }
        return builder.toString();
    }

}
