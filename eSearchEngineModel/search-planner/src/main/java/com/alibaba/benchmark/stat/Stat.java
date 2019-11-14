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

import java.util.ArrayList;
import java.util.List;
import java.util.LongSummaryStatistics;

/**
 * @author CarpenterLee
 */
public class Stat {
    public static void main(String[] args) {
        LongSummaryStatistics s = new LongSummaryStatistics();
        s.accept(10);
    }

    private List<Long> list = new ArrayList<>();
    private LongSummaryStatistics statistics = new LongSummaryStatistics();

    public void accept(long v) {
        list.add(v);
        statistics.accept(v);
    }

    public void clear() {
        list.clear();
        statistics = new LongSummaryStatistics();
    }

    public int size() {
        return list.size();
    }

    public long percentile(double percent) {
        int i = (int)(list.size() * percent) - 1;
        list.sort(Long::compare);
        if (i >= 0 && i < list.size()) {
            return list.get(i);
        }
        return -1;
    }

    public long max() {
        return statistics.getMax();
    }

    public long min() {
        return statistics.getMin();
    }

    public double avg() {
        return statistics.getAverage();
    }

}
