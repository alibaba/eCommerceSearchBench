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

package com.alibaba.qp.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;
import com.mayabot.mynlp.fasttext.FastText;
import com.mayabot.mynlp.fasttext.FloatStringPair;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

/**
 * @author CarpenterLee
 */
@Service
public class CategoryService {
    private Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Value("${fasttext.model.path}")
    private String modelPath;
    @Value("${stopWords.path}")
    private String stopWordsPath;
    @Autowired
    private ResourceLoader resourceLoader;

    private FastText fastText;
    private JiebaSegmenter segmenter;
    private Set<String> stopWords;

    @PostConstruct
    void init() throws Exception {
        //Resource resource = resourceLoader.getResource(modelPath);
        logger.info("fastText model loading...");
        fastText = FastText.loadFasttextBinModel(modelPath);
        logger.info("fastText model loaded!");
        segmenter = new JiebaSegmenter();
        initStopWords();
    }

    private void initStopWords() throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            resourceLoader.getResource(stopWordsPath).getInputStream()));
        String line;
        stopWords = new HashSet<>();
        while ((line = reader.readLine()) != null) {
            stopWords.add(line);
        }
    }

    public String predict(String query) {
        List<String> tokens = parse(query);
        List<FloatStringPair> predict = fastText.predict(tokens, 1);
        return predict.get(0).second;
    }

    private List<String> parse(String query) {
        List<SegToken> tokens = segmenter.process(query, SegMode.SEARCH);
        return tokens.stream()
            .map(t -> t.word)
            .filter(t -> !stopWords.contains(t))
            .collect(Collectors.toList());
    }

}
