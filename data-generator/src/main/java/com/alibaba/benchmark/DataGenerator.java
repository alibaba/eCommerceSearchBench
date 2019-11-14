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

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.alibaba.benchmark.KeyGenerator.makeKey;

class DataGenerator {
    static void generateUserData(int scale, String out) {
        int baseNum = (int) 6e3;
        int userNum = baseNum * scale;
        Random random = new Random(0);
        final String us = "\u001f";
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(out))) {
            int sexMin = 0;
            int sexMax = 1;
            int ageMin = 0;
            int ageMax = 149;
            int powerMin = 0;
            int powerMax = 6;

            for (int i = 0; i < userNum; i++) {
                int sex = sexMin + (int) (random.nextDouble() * (sexMax - sexMin + 1));
                int age = ageMin + (int) (random.nextDouble() * (ageMax - ageMin + 1));
                int power = powerMin + (int) (random.nextDouble() * (powerMax - powerMin + 1));
                writer.write(String.join(us, String.valueOf(i), String.valueOf(sex), String.valueOf(age), String.valueOf(power)));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void generateItemData(int scale, Item item, String itemDistributionFilePath, String itemDataOutputPath) {
        int baseNum = (int) 1e4;
        int itemNum = baseNum * scale;
        int itemIdMin = 0, itemIdMax = itemNum;
        Random random = new Random(0);
        final String US = "\u001f";

        switch (item) {
            case EXCELLENT:
                itemIdMin = 0;
                itemIdMax = (int) (0.15 * itemNum);
                random = new Random(1);
                break;
            case GOOD:
                itemIdMin = (int) (0.15 * itemNum);
                itemIdMax = (int) (0.5 * itemNum);
                random = new Random(1);
                break;
            case BAD:
                itemIdMin = (int) (0.5 * itemNum);
                itemIdMax = itemNum;
                random = new Random(2);
                break;
        }
        int categoryMinLimit = 0;
        int categoryMaxLimit = (int) 1e6;
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(itemDataOutputPath))) {
            SentenceDistribution sentenceDistribution = DistributionUtils.getSentenceDistribution(itemDistributionFilePath);
            for (int id = itemIdMin; id < itemIdMax; id++) {
                String title = sentenceDistribution.pickSentence(random);

                double price = random.nextDouble() * random.nextInt();
                int rateSum = Math.abs(random.nextInt());
                int category = categoryMinLimit + (int) (random.nextDouble() * (categoryMaxLimit - categoryMinLimit));
                String relate_items = "";

                List<String> storedData = new ArrayList<>();
                for (int j = 0; j < 26; j++) {
                    storedData.add(makeKey(random.nextLong()));
                }
//                String id = makeKey(random.nextLong());
//                String id = String.valueOf(id);

                List<String> properties = new ArrayList<>();
                properties.add(title);
                properties.add(String.valueOf(price));
                properties.add(String.valueOf(rateSum));
                properties.add(String.valueOf(category));
                properties.add(relate_items);
                properties.addAll(storedData);
                properties.add(String.valueOf(id));

                writer.write(String.join(US, properties));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
