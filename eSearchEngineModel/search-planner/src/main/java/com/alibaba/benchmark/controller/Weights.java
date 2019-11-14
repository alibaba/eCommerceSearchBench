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

package com.alibaba.benchmark.controller;

import java.io.Serializable;
import java.util.List;

public class Weights implements Serializable {

    private List<List<Double>> predictions;

    public List<List<Double>> getPredictions() {
        return predictions;
    }

    public void setPredictions(List<List<Double>> predictions) {
        this.predictions = predictions;
    }

    public List<Double> getWeights() {
        return predictions.get(0);
    }
}
