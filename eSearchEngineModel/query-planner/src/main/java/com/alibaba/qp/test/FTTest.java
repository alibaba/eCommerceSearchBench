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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.mayabot.mynlp.fasttext.FastText;
import com.mayabot.mynlp.fasttext.FloatStringPair;
import com.mayabot.mynlp.fasttext.ModelName;

/**
 * Refer:<br/>
 * https://github.com/mayabot/fastText4j <br/>
 * https://github.com/facebookresearch/fastText
 *
 * @author CarpenterLee
 */
public class FTTest {
    public static void main(String[] args) throws Exception {
        FTTest ftTest = new FTTest();
        //String f = "/Users/lh/Downloads/mainse_lables.txt";
        //ftTest.train(f);
        //String f = "/Users/lh/Downloads/query-planner/src/main/resources/model1";
        //ftTest.load(f);
        ftTest.validate();
        System.out.println("end~");

    }

    private void train(String trainFile) throws Exception {
        FastText train = FastText.train(new File(trainFile), ModelName.sup);
        System.out.println(train.predict(Arrays.asList("喜欢ni"), 1));
        train.saveModel("/Users/lh/Downloads/query-planner/src/main/resources/model.model1");
    }

    private void load(String p) throws Exception {
        FastText fastText = FastText.loadModel(p, true);
        System.out.println(fastText.predict(Arrays.asList("小学生", "同义词", "反义词", "手册"), 3));
    }

    private void validate() throws Exception {
        String modelPath = "/Users/lh/Downloads/query-planner/src/main/resources/model_than100.bin";
        String validateFile = "/Users/lh/Downloads/query-planner/src/main/resources/split_labels_all.txt";
        FastText fastText = FastText.loadFasttextBinModel(modelPath);
        AtomicLong lines = new AtomicLong();
        AtomicLong rightLines = new AtomicLong();
        AtomicLong rightInTop3 = new AtomicLong();
        System.out.println("model loaded!");
        long s = System.currentTimeMillis();
        Files.lines(new File(validateFile).toPath(), Charset.forName("utf-8"))
            .parallel()
            .forEach(line -> {
                int i = line.indexOf(" ");
                String targetLabel = line.substring(0, i);
                String[] toPredict = line.substring(i + 1).split(" ");
                lines.incrementAndGet();
                List<FloatStringPair> predict = fastText.predict(Arrays.asList(toPredict), 6);
                String label = predict.get(0).second;
                if (targetLabel.equals(label)) {
                    rightLines.incrementAndGet();
                    rightInTop3.incrementAndGet();
                }
                if (targetLabel.equals(predict.get(1).second)
                    || targetLabel.equals(predict.get(2).second)) {
                    rightInTop3.incrementAndGet();
                }
                if (lines.get() % 1000 == 0) {
                    long c = System.currentTimeMillis() - s;
                    double speed = 1000.0 * lines.get() / c;
                    System.out.println(lines + ", " + rightLines + ", " + rightInTop3 + ", "
                        + String.format("%.2f", speed));
                }
            });
        double ratio = 1.0 * rightLines.get() / lines.get();
        System.out.println("lines: " + lines + ", right: " + rightLines + ", ratio: " + ratio);
        long c = System.currentTimeMillis() - s;
        double speed = 1000.0 * lines.get() / c;
        System.out.println("speed: " + speed);

    }
}
