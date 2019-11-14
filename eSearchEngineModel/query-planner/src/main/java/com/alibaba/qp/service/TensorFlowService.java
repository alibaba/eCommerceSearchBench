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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * @author CarpenterLee
 */
@Service
public class TensorFlowService {

    private Logger logger = LoggerFactory.getLogger(TensorFlowService.class);

    @Value("${tensorFlow.address}")
    private String tfAddress;

    public static void main(String[] args) {
        TensorFlowService service = new TensorFlowService();
        service.tfAddress = "http://www.baidu.com";
        service.userWight(1, 1, 1, 1);
    }

    public String userWight(int category, int sex, int age, int power) {
        String base = "{\"instances\":[{\"sex\":[%d],\"power\":[%d],\"category\":[%d],\"age\":[%d]}]}";
        String payload = String.format(base, sex, power, category, age);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(payload, headers);
        String weights = restTemplate.postForObject(tfAddress, request, String.class);
        //logger.info("weights={}", weights);
        return weights;
    }
}
