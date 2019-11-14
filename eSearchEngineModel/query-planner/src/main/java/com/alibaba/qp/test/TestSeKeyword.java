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

package com.alibaba.qp.test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

/**
 * @author CarpenterLee
 */
public class TestSeKeyword {
    public static void main(String[] args) throws Exception {
        System.out.println("begin...");
        long st = System.currentTimeMillis();
        TestSeKeyword testSeKeyword = new TestSeKeyword();
        //System.out.println(testSeKeyword.pattern.matcher("￥bkoe0osth5k￥,1").matches());
        //testSeKeyword.parseProperty();
        testSeKeyword.splitKeyword("/Users/lh/Desktop/tail10000.txt",
            "/Users/lh/eclipse-workspace/tf/aliesearch/query-planner/src/main/resources/stop_words.txt");
        long cost = System.currentTimeMillis() - st;
        System.out.println("end! cost=" + cost + " ms");
    }

    private void splitKeyword(String inFileName, String stopWordFileName) throws Exception {
        Set<String> stopWords = Files.lines(new File(stopWordFileName).toPath()).collect(Collectors.toSet());
        JiebaSegmenter segmenter = new JiebaSegmenter();
        Files.lines(new File(inFileName).toPath()).forEach(line -> {
            int i = line.lastIndexOf(",");
            int frequency = Integer.parseInt(line.substring(i + 1));
            String keyword = line.substring(0, i);
            List<SegToken> tokens = segmenter.process(keyword, SegMode.SEARCH);
            System.out.println(keyword);
            for (SegToken token : tokens) {
                System.out.println(token);
            }
            System.out.println("#######");
        });

    }

    private void parseProperty() throws Exception {
        String name = "/Users/lh/Desktop/mainse_keyword_part_20180301.txt";
        String outName = "/Users/lh/Desktop/out.txt";
        final int minFrequency = 1;
        Map<String, AtomicLong> counters = new HashMap<>();
        long lines = 0;
        long linesWithProp = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(name)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int i = line.lastIndexOf(",");
                int frequency = Integer.parseInt(line.substring(i + 1));
                if (frequency < minFrequency) {
                    break;
                }
                String[] strs = line.substring(0, i).split(" ");
                if (strs.length > 1) {
                    linesWithProp++;
                }
                for (int j = 1; j < strs.length; j++) {
                    String pro = strs[j];
                    if (pro.isEmpty()) {
                        continue;
                    }
                    counters.computeIfAbsent(pro, s -> new AtomicLong()).addAndGet(frequency);
                }
                lines++;
                if (lines % 100000 == 0) {
                    System.out.println(lines + ", " + line + ", counters.size=" + counters.size());
                }
            }
        }
        System.out.println();
        System.out.println(lines + ", counters.size=" + counters.size());
        System.out.println("linesWithProp=" + linesWithProp);
        ArrayList<Entry<String, AtomicLong>> entries = new ArrayList<>(counters.entrySet());
        entries.sort((a, b) -> Long.compare(b.getValue().get(), a.getValue().get()));
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outName)))) {
            for (Entry<String, AtomicLong> entry : entries) {
                writer.append(entry.getKey())
                    .append(",")
                    .append(entry.getValue().toString())
                    .append("\n");
                //if(entry.getValue().get() < 10){
                //    break;
                //}
            }
        }
        //entries.stream()
        //    .map(a -> a.getValue().get())
        //    .collect(Collectors.groupingBy(a -> a))
        //    .forEach((k, v) -> {
        //        System.out.println(k + ", " + v.size());
        //    });
    }

    private void f() throws Exception {
        String name = "/Users/lh/Desktop/mainse_keyword_part_20180301.txt";
        Stream<String> lines = Files.lines(new File(name).toPath());
        long count = lines.parallel().filter(this::isInvalid).count();
        System.out.println("count=" + count);
    }

    //￥bkoe0osth5k￥,1
    Pattern pattern = Pattern.compile("￥([a-z0-9]){8,11}.*");

    private boolean isInvalid(String s) {
        if (s.length() > 50) {
            return true;
        }
        if (pattern.matcher(s).matches()) {
            return true;
        }
        return false;
    }
}
