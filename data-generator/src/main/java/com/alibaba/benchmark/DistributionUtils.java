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

package com.alibaba.benchmark;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DistributionUtils {

    static SentenceDistribution getSentenceDistribution(String sentenceFilePath) {
        Map<Integer, Integer> sentenceLengthFrequency = new HashMap<>();
        Map<String, Integer> wordFrequency = new HashMap<>();
        final String us = "\u001F";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(sentenceFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] titles = line.split(us, -1);
                String title_segment = titles[1].replaceAll("\\s+", " ");
                String[] segments = title_segment.split(" ");
                int sentenceLength = segments.length;
                if (!sentenceLengthFrequency.containsKey(sentenceLength)) {
                    sentenceLengthFrequency.put(sentenceLength, 0);
                }
                sentenceLengthFrequency.put(sentenceLength, sentenceLengthFrequency.get(sentenceLength) + 1);

                for (String segment : segments) {
                    if (!wordFrequency.containsKey(segment)) {
                        wordFrequency.put(segment, 0);
                    }
                    wordFrequency.put(segment, wordFrequency.get(segment) + 1);
                }
            }
//            System.out.println(sentenceLengthFrequency);
//            System.out.println(wordFrequency);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new SentenceDistribution(getWeights(sentenceLengthFrequency), getWeights(wordFrequency));
    }

    static <E> List<Map.Entry<E, Double>> getWeights(Map<E, Integer> frequency) {
        List<Map.Entry<E, Double>> weights = new ArrayList<>();
        int total = frequency.values().stream().mapToInt(Integer::intValue).sum();
        int cumSum = 0;
        for (E e : frequency.keySet()) {
            cumSum += frequency.get(e);
            double cumWeight = 1.0 * cumSum / total;
            weights.add(new AbstractMap.SimpleEntry<>(e, cumWeight));
        }

        return weights;
    }
}
