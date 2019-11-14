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

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.WrapperQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class DataTransformUtils {

    void splitItems(String itemPath, String returnItemPath) {
        try (
                Stream<String> itemStream = Files.lines(Paths.get(itemPath));
                Stream<String> itemReturnsStream = Files.lines(Paths.get(returnItemPath));
                BufferedWriter excellentWriter = new BufferedWriter(Files.newBufferedWriter(Paths.get("excellentItem")));
                BufferedWriter goodWriter = new BufferedWriter(Files.newBufferedWriter(Paths.get("goodItem")));
                BufferedWriter badWriter = new BufferedWriter(Files.newBufferedWriter(Paths.get("badItem")))
        ) {
            double goodRatio = 0.54;
            double excellentRatio = 0.15 * goodRatio;
            double badRatio = 1 - goodRatio;
            Set<Integer> goodItemSet = new HashSet<>();
            String[] items = itemStream.toArray(String[]::new);
            String[] itemReturns = itemReturnsStream.toArray(String[]::new);
            int itemReturnTotal = itemReturns.length;
            for (int i = 0; i < itemReturnTotal; i++) {
                int itemId = Integer.parseInt(itemReturns[i].split(",")[0]);
                String item = items[itemId];
                if (i < itemReturnTotal * excellentRatio) {
                    excellentWriter.write(item);
                    excellentWriter.newLine();
                    goodWriter.write(item);
                    goodWriter.newLine();
                    goodItemSet.add(itemId);
                } else if (i < itemReturnTotal * goodRatio) {
                    goodWriter.write(item);
                    goodWriter.newLine();
                    goodItemSet.add(itemId);
                }
            }
            for (int i = 0; i < items.length; i++) {
                if (!goodItemSet.contains(i)) {
                    badWriter.write(items[i]);
                    badWriter.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void parseItemInfo(String dir, String pattern, String outPath) {
        final String docStart = "<doc>";
        final String docEnd = "</doc>";
        final String delimiter = "\001\n";

        Set<String> fields = new HashSet<>();
        Map<String, Integer> categories = new HashMap<>();
        int categoryIndex = 0;

        long count = 0;
        try (
                DirectoryStream<Path> dirStream = Files.newDirectoryStream(Paths.get(dir), pattern);
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(outPath))
        ) {
            for (Path path : dirStream) {
                try (Scanner scanner = new Scanner(new GZIPInputStream(new FileInputStream(path.toString()), 1024*1024))) {

                    scanner.useDelimiter(delimiter);
                    while (scanner.hasNext()) {
                        String record = scanner.next();
                        if (!(record.equals(docStart) || record.equals(docEnd))) {
                            String[] keyValue = record.split("=", 2);
                            String key = keyValue[0];
                            String value = keyValue[1];

                            fields.add(key);

                            if (key.equals("title")) {
                                writer.write(value.replaceAll("\\s+", " "));
                            }
                            if (key.equals("category")) {
                                if (!categories.containsKey(value)) {
                                    categories.put(value, categoryIndex);
                                    categoryIndex++;
                                }
                                writer.write("\t__label__" + categories.get(value));
                                writer.newLine();
                                count++;
                                if(count % 1000 == 0){
                                    System.out.println("line count: " + count);
                                    writer.flush();
                                }
                            }
                        }
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void parseSegmentData(String path, String out) {
        Map<String, Integer> dictionary = new HashMap<>();
        int dictIndex = 0;
        final String us = "\u001F";
        try (
                BufferedReader reader = Files.newBufferedReader(Paths.get(path));
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(out))
        ) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] titles = line.split(us, -1);
                String title_segment = titles[2].replaceAll("\\s+", " ");
                for (String segment : title_segment.split(" ")) {
                    if (!dictionary.containsKey(segment)) {
                        dictionary.put(segment, dictIndex);
                        dictIndex++;
                    }
                }
            }

            for (String word : dictionary.keySet()) {
                writer.write(String.join(us, word, String.valueOf(dictionary.get(word))));
                writer.newLine();
            }

            System.out.println(dictionary.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void anonymous(String in, String out) {
        Map<String, Integer> idNo = new HashMap<>();
        int no = 0;
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(in));
             BufferedWriter writer = Files.newBufferedWriter(Paths.get(out))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                String id = values[0];
                if (!idNo.containsKey(id)) {
                    idNo.put(id, no);
                    no++;
                }
                writer.write(String.join(",", String.valueOf(idNo.get(id)), values[1], values[2]));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void parseDict(String in, String out) {
        List<Integer> counts = new ArrayList<>();
        final String us = "\u001f";
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] wordCount = line.split(us);
                counts.add(Integer.valueOf(wordCount[1]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(counts);
        counts.forEach(System.out::println);
    }

    void mergeDict(String queryDictPath, String itemDictPath, String out) {
        final String us = "\u001f";
        int no = 0;
        Map<String, Integer> dict = new HashMap<>();
        try (
                BufferedReader queryDictReader = Files.newBufferedReader(Paths.get(queryDictPath));
                BufferedReader itemDictReader = Files.newBufferedReader(Paths.get(itemDictPath));
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(out))) {
            BufferedReader[] readers = new BufferedReader[]{queryDictReader, itemDictReader};
            String line;
            for (BufferedReader reader : readers) {
                while ((line = reader.readLine()) != null) {
                    String[] wordNo = line.split(us, -1);
                    String word = wordNo[0];
                    if (!dict.containsKey(word)) {
                        dict.put(word, no);
                        no++;
                    }
                }
            }
            for (String word : dict.keySet()) {
                writer.write(String.join(us, word, dict.get(word).toString()));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void query(String queryStr) {
        try {
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost("10.101.220.23", 9200, "http")));
            SearchRequest request = new SearchRequest();
            request.indices("items");
            SearchSourceBuilder builder = new SearchSourceBuilder();
            builder.query(new WrapperQueryBuilder(queryStr));
            System.out.println(builder.toString());
            request.source(builder);
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            for (SearchHit hit : response.getHits().getHits()) {
                System.out.println(hit.getId());
            }
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void transformSearchLogFormat(String searchLogPath, String transformedSearchLogPath) {
        final String DELIMITER = "\t";
        try (
                BufferedReader logReader = Files.newBufferedReader(Paths.get(searchLogPath));
                BufferedWriter logWriter = Files.newBufferedWriter(Paths.get(transformedSearchLogPath))
        ) {
            Map<String, Integer> userToId = new HashMap<>();
            int idx = 0;
            String log;
            while ((log = logReader.readLine()) != null) {
                String[] uidQueryPage = log.split(DELIMITER);
                String uid = uidQueryPage[0];
                String query = uidQueryPage[1];
                String page = uidQueryPage[2];

                if (!userToId.containsKey(uid)) {
                    userToId.put(uid, idx);
                    idx++;
                }
                logWriter.write(String.join(DELIMITER, String.valueOf(userToId.get(uid)), query, page));
                logWriter.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
