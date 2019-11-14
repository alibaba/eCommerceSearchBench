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

package com.alibaba.qp.controller;

import com.alibaba.qp.bo.Query;
import com.alibaba.qp.bo.User;
import com.alibaba.qp.service.AnalyseQueryService;
import com.alibaba.qp.service.CategoryService;
import com.alibaba.qp.service.TensorFlowService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author CarpenterLee
 */
@RestController
public class QueryPlannerController {
    Logger logger = LoggerFactory.getLogger(QueryPlannerController.class);
    @Autowired
    private AnalyseQueryService analyseQueryService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TensorFlowService tensorFlowService;

    @RequestMapping("/checkpreload.htm")
    public String health() {
        return "success";
    }

    @RequestMapping("/")
    public String hello() {
        return "hello " + System.currentTimeMillis();
    }

    @PostMapping("/query")
    public Object query(@RequestBody Query queryBody) {
        long totalSt = System.currentTimeMillis();
        if (queryBody == null) {
            throw new RuntimeException("queryBody.null");
        }
        String uid = queryBody.getUid();
        String query = queryBody.getQuery();
        if (uid == null) {
            throw new RuntimeException("uid.null");
        }
        if (query == null) {
            throw new RuntimeException("query.null");
        }
        long st = System.currentTimeMillis();
        User user = analyseQueryService.analyseQuery(uid);
        long analyseQueryTime = System.currentTimeMillis() - st;
        if (user == null) {
            throw new RuntimeException("user.null");
        }
        st = System.currentTimeMillis();
        String label = categoryService.predict(query);
        long predictTime = System.currentTimeMillis() - st;
        st = System.currentTimeMillis();
        int category = Integer.parseInt(label.substring("__label__".length()));
        String wight = tensorFlowService.userWight(category, user.getSex(), user.getAge(), user.getPower());
        long userWightTime = System.currentTimeMillis() - st;
        long totalCost = System.currentTimeMillis() - totalSt;
        //String out = String.format("analyseQueryTime=%d, predictTime=%d, userWightTime=%d, totalTime=%d"
        //    ,analyseQueryTime, predictTime, userWightTime, totalCost);
        String out = "analyseQueryTime=" + analyseQueryTime
            + ", predictTime=" + predictTime
            + ", userWightTime=" + userWightTime
            + ", totalTime=" + totalCost;
        //System.out.println(out);
        logger.info(out);
        return wight;
    }
}
