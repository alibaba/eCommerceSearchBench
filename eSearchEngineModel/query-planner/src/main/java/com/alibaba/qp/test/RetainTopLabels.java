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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author CarpenterLee
 */
public class RetainTopLabels {
    public static void main(String[] args) throws Exception {
        System.out.println("start...");
        new RetainTopLabels().f();
    }

    void f() throws Exception {
        String inStr = "/Users/lh/Downloads/query-planner/src/main/resources/split_labels_all.txt";
        String outStr = "/Users/lh/Downloads/query-planner/src/main/resources/split_labels_than100.txt";
        Map<String, AtomicLong> counters = new ConcurrentHashMap<>();
        Files.lines(new File(inStr).toPath())
            .parallel()
            .forEach(line -> {
                String label = line.substring(0, line.indexOf(" "));
                counters.computeIfAbsent(label, a -> new AtomicLong())
                    .incrementAndGet();
            });
        Set<String> labelsToRetain = new HashSet<>();
        final long threshold = 100;
        counters.forEach((k, v) -> {
            if (v.get() >= threshold) {
                labelsToRetain.add(k);
            }
        });
        AtomicLong allLines = new AtomicLong();
        AtomicLong retainLines = new AtomicLong();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outStr)))) {
            Files.lines(new File(inStr).toPath())
                .forEach(line -> {
                    allLines.incrementAndGet();
                    String label = line.substring(0, line.indexOf(" "));
                    if (labelsToRetain.contains(label)) {
                        retainLines.incrementAndGet();
                        try {
                            writer.append(line);
                            writer.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        }
        System.out.println("allLines=" + allLines + ", retainLines=" + retainLines);
        System.out.println("end~");

    }
}
