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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

public class SentenceDistribution {
    private List<Map.Entry<Integer, Double>> sentenceLengthWeights;
    private List<Map.Entry<String, Double>> wordWeights;

    public SentenceDistribution(List<Map.Entry<Integer, Double>> sentenceLengthWeights,
                                List<Map.Entry<String, Double>> wordWeights) {
        this.sentenceLengthWeights = sentenceLengthWeights;
        this.wordWeights = wordWeights;
    }

    String pickSentence(Random random) {
        int length = pickLength(random);
        List<String> words = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            words.add(pickWord(random));
        }

        return String.join("", words);
    }

    private int pickLength(Random random) {
        double rand = random.nextDouble();
        return pickValue(sentenceLengthWeights, rand);
    }

    private String pickWord(Random random) {
        double rand = random.nextDouble();
        return pickValue(wordWeights, rand);
    }

    private <E> E pickValue(List<Map.Entry<E, Double>> valueWeights, double rand) {
        Entry<E, Double> entry = new AbstractMap.SimpleEntry<>(null, rand);
        int i = Collections.binarySearch(valueWeights, entry, Comparator.comparingDouble(Entry::getValue));
        if (i >= 0) {
            return valueWeights.get(i).getKey();
        }
        int y = -i - 1;
        if(y >= 0 && y < valueWeights.size()){
            return valueWeights.get(y).getKey();
        }
        return valueWeights.get(valueWeights.size() - 1).getKey();

        //for (Map.Entry<E, Double> valueWeight : valueWeights) {
        //    if (rand < valueWeight.getValue()) {
        //        return valueWeight.getKey();
        //    }
        //}
        //return valueWeights.get(valueWeights.size() - 1).getKey();
    }
}
