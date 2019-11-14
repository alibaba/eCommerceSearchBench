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

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

import static org.neo4j.driver.v1.Values.parameters;

public class DataLoader {
    public static void loadItemData(String path, RestHighLevelClient client) {
        final String docStart = "<doc>";
        final String docEnd = "</doc>";
        final String delimiter = "\001\n";

        try (
            Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(path)))
        ) {
            scanner.useDelimiter(delimiter);

            Optional<String> nid = Optional.empty();
            Optional<String> title = Optional.empty();
            Optional<Double> price = Optional.empty();
            Optional<Integer> rateSum = Optional.empty();
            Optional<String> category = Optional.empty();
            Optional<List<String>> relatedItems = Optional.empty();

            BulkProcessor.Listener listener = new BulkProcessor.Listener() {
                @Override
                public void beforeBulk(long executionId, BulkRequest request) {

                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

                }

                @Override
                public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

                }
            };

            BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = ((request, bulkListener) ->
                client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener));
            BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener).build();

            while (scanner.hasNext()) {
                String record = scanner.next();
                if (record.equals(docEnd)) {
                    if (nid.isPresent() && title.isPresent() && price.isPresent() && rateSum.isPresent() && category
                        .isPresent()) {
                        IndexRequest request = new IndexRequest("items", "_doc", nid.get())
                            .source(XContentType.JSON,
                                "title", title.get(),
                                "price", price.get(),
                                "ratesum", rateSum.get(),
                                "category", category.get(),
                                "related_items", relatedItems.orElseGet(ArrayList::new));
                        bulkProcessor.add(request);
                    }
                }
                if (!(record.equals(docStart) || record.equals(docEnd))) {
                    String[] keyValue = record.split("=", 2);
                    String key = keyValue[0];
                    String value = keyValue[1];

                    switch (key) {
                        case "nid":
                            nid = Optional.of(value);
                            break;
                        case "title":
                            title = Optional.of(value);
                            break;
                        case "ct_i2i_c2c":
                            List<String> items = new ArrayList<>();
                            String[] values = value.split("\\s");
                            for (int i = 0; i < values.length; i++) {
                                if (i % 2 == 0) {
                                    if (!values[i].isEmpty()) {
                                        items.add(values[i]);
                                    }
                                }
                            }
                            relatedItems = Optional.of(items);
                            break;
                        case "price":
                            price = Optional.of(Double.valueOf(value));
                            break;
                        case "ratesum":
                            rateSum = Optional.of(Integer.valueOf(value));
                            break;
                        case "category":
                            category = Optional.of(value);
                            break;
                    }
                }
            }

            boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
            while (!terminated) {
                terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void loadUserData(String uri, String in) {
        String us = "\u001f";
        try (
            BufferedReader reader = Files.newBufferedReader(Paths.get(in));
            Driver driver = GraphDatabase.driver(uri);
            Session session = driver.session()
        ) {
            session.run("CREATE INDEX ON :User(uid)");
            String line;
            long linesCount = 0;
            while ((line = reader.readLine()) != null) {
                String[] userInfo = line.split(us);
                String uid = userInfo[0];
                int sex = Integer.parseInt(userInfo[1]);
                int age = Integer.parseInt(userInfo[2]);
                int power = Integer.parseInt(userInfo[3]);

                session.run(
                    "CREATE (user:User {uid: $uid, sex: $sex, age: $age, power: $power})",
                    parameters("uid", uid, "sex", sex, "age", age, "power", power));
                linesCount++;
                if (linesCount % 10000 == 0) {
                    System.out.println(linesCount + " users loaded by now");
                }
            }
            System.out.println("all users loaded: " + linesCount);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void loadIndex(String in, RestHighLevelClient client, String index, int mode) {

        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest request) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {

            }

            @Override
            public void afterBulk(long executionId, BulkRequest request, Throwable failure) {

            }
        };

        BiConsumer<BulkRequest, ActionListener<BulkResponse>> bulkConsumer = ((request, bulkListener) ->
            client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener));
        BulkProcessor bulkProcessor = BulkProcessor.builder(bulkConsumer, listener).build();

        final String us = "\u001f";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(in))) {
            String line;
            String title;
            String price;
            String rateSum;
            String category;
            String relateItems;
            String a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
            String id;

            while ((line = reader.readLine()) != null) {
                String[] properties = line.split(us, -1);
                title = properties[0];
                price = properties[1];
                rateSum = properties[2];
                category = properties[3];
                relateItems = properties[4];
                a = properties[5];
                b = properties[6];
                c = properties[7];
                d = properties[8];
                e = properties[9];
                f = properties[10];
                g = properties[11];
                h = properties[12];
                i = properties[13];
                j = properties[14];
                k = properties[15];
                l = properties[16];
                m = properties[17];
                n = properties[18];
                o = properties[19];
                p = properties[20];
                q = properties[21];
                r = properties[22];
                s = properties[23];
                t = properties[24];
                u = properties[25];
                v = properties[26];
                w = properties[27];
                x = properties[28];
                y = properties[29];
                z = properties[30];
                id = properties[31];

                switch (mode) {
                    case 0: {
                        IndexRequest request = new IndexRequest(index, "_doc", id)
                            .source(XContentType.JSON,
                                "title", title,
                                "related_items", relateItems);
                        bulkProcessor.add(request);
                        break;
                    }
                    case 1: {
                        IndexRequest request = new IndexRequest(index, "_doc", id)
                            .source(XContentType.JSON,
                                "title", title,
                                "price", price,
                                "ratesum", rateSum,
                                "category", category);
                        bulkProcessor.add(request);
                        break;
                    }
                    case 2: {
                        IndexRequest request = new IndexRequest(index, "_doc", id)
                            .source(XContentType.JSON,
                                "title", title,
                                "price", price,
                                "ratesum", rateSum,
                                "category", category,
                                "related_items", relateItems,
                                "a", a,
                                "b", b,
                                "c", c,
                                "d", d,
                                "e", e,
                                "f", f,
                                "g", g,
                                "h", h,
                                "i", i,
                                "j", j,
                                "k", k,
                                "l", l,
                                "m", m,
                                "n", n,
                                "o", o,
                                "p", p,
                                "q", q,
                                "r", r,
                                "s", s,
                                "t", t,
                                "u", u,
                                "v", v,
                                "w", w,
                                "x", x,
                                "y", y,
                                "z", z);
                        bulkProcessor.add(request);
                        break;
                    }
                }
            }

            boolean terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
            while (!terminated) {
                terminated = bulkProcessor.awaitClose(30L, TimeUnit.SECONDS);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
