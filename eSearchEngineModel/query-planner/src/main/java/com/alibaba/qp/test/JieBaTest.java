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

import java.util.List;

import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.JiebaSegmenter.SegMode;
import com.huaban.analysis.jieba.SegToken;

/**
 * https://github.com/huaban/jieba-analysis
 *
 * @author CarpenterLee
 */
public class JieBaTest {
    public static void main(String[] args) {
        //char c = '\uD83D';
        //char c = '\uDC49';
        char c = '中';
        System.out.println(Character.isSurrogate(c));

        JiebaSegmenter segmenter = new JiebaSegmenter();
        //String str = "我来到北京清华大学";
        String str = "这条信息￥ez9q0oflvht￥后打开\uD83D\uDC49手淘\uD83D\uDC48 ";
        List<SegToken> tokens = segmenter.process(str, SegMode.SEARCH);
        for (SegToken token : tokens) {
            System.out.println(token);
        }
    }
}
