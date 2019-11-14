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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import com.alibaba.qp.bo.User;

import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * @author CarpenterLee
 */
@Service
public class AnalyseQueryService {

    private Driver driver;

    @Value("${neo4j.addresss}")
    private String neo4jUrl;

    public static void main(String[] args) {
        AnalyseQueryService service = new AnalyseQueryService();
        service.init();
        User user = service.analyseQuery("1");
        System.out.println(user);
    }

    @PostConstruct
    public void init() {
        driver = GraphDatabase.driver(neo4jUrl);
    }

    public User analyseQuery(final String uid) {
        try (Session session = driver.session()) {
            User user = session.writeTransaction(tx -> {
                Map<String, Object> map = new HashMap<>();
                map.put("uid", uid);
                StatementResult result = tx.run("MATCH (user:User {uid: $uid}) RETURN user", map);
                if (result.hasNext()) {
                    org.neo4j.driver.v1.Value value = result.list().get(0).get(0);
                    String uid1 = value.get("uid").asString();
                    int sex = value.get("sex").asInt();
                    int age = value.get("age").asInt();
                    int power = value.get("power").asInt();
                    return new User(uid1, sex, age, power);
                }
                return null;
            });
            return user;
        }
    }
}
