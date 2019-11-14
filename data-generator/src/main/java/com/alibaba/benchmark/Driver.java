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

public class Driver {
    public static void main(String[] args) {
        //DataTransformUtils.parseItemInfo("/Users/tangfei", "mainse_lasttable.gz", "mainse_lables");
        System.out.println("start....");
        long s = System.currentTimeMillis();
        DataTransformUtils.parseItemInfo("/Users/lh/Downloads",
            "mainse_lasttable3.gz",
            "/Users/lh/Downloads/mainse_lables3");
        long t = System.currentTimeMillis() - s;
        System.out.println("time: " + t + " ms");
    }
}
