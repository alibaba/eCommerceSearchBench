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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import picocli.CommandLine;

import java.io.IOException;

import static com.alibaba.benchmark.DataGenerator.*;

public class BenchmarkCli {

    @CommandLine.Command(name = "benchmark-cli", description = "benchmark command line application",
        subcommands = {Search.class, Generator.class, Transformer.class})
    static class Benchmark implements Runnable {

        @Override
        public void run() {
            new CommandLine(new Benchmark()).usage(System.out);
        }
    }

    @CommandLine.Command(name = "load", description = "load data")
    static class Search implements Runnable {

        @CommandLine.Option(names = "--excellent-item-host",
            description = "host name of search engine storing excellent items", required = true)
        private String excellentItemHost;

        @CommandLine.Option(names = "--excellent-item-port",
            description = "port of search engine storing excellent items", defaultValue = "9200")
        private int excellentItemPort;

        @CommandLine.Option(names = "--good-item-host", description = "host name of search engine storing good items",
            required = true)
        private String goodItemHost;

        @CommandLine.Option(names = "--good-item-port", description = "port of search engine storing good items",
            defaultValue = "9200")
        private int goodItemPort;

        @CommandLine.Option(names = "--bad-item-host", description = "host name of search engine storing bad items",
            required = true)
        private String badItemHost;

        @CommandLine.Option(names = "--bad-item-port", description = "port of search engine storing bad items",
            defaultValue = "9200")
        private int badItemPort;

        @CommandLine.Option(names = "--ranking-host", description = "host name of ranking system", required = true)
        private String rankingHost;

        @CommandLine.Option(names = "--ranking-port", description = "port of ranking system", defaultValue = "9200")
        private int rankingPort;

        @CommandLine.Option(names = "--summary-host", description = "summary host", required = true)
        private String summaryHost;

        @CommandLine.Option(names = "--summary-port", description = "port of summary system", defaultValue = "9200")
        private int summaryPort;

        //        @CommandLine.Option(names = "-p", description = "search engine port", defaultValue = "9200")
        //        private int port;

        @CommandLine.Option(names = "-s", description = "search engine access protocol", required = true,
            defaultValue = "http")
        private String schema;

        @CommandLine.Option(names = "--user", description = "user data output path", defaultValue = "user_data.txt")
        private String userDataPath;

        @CommandLine.Option(names = "--excellent-item", description = "excellent item data output path",
            defaultValue = "excellent_item.txt")
        private String excellentItemDataPath;

        @CommandLine.Option(names = "--good-item", description = "good item data output path",
            defaultValue = "good_item.txt")
        private String goodItemDataPath;

        @CommandLine.Option(names = "--bad-item", description = "bad item data output path",
            defaultValue = "bad_item.txt")
        private String badItemDataPath;

        @CommandLine.Option(names = "--neo4j-uri", description = "neo4j uri", required = true)
        private String neo4jURI;

        @Override
        public void run() {
            RestHighLevelClient excellentItemSearchEngineClient = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(excellentItemHost, excellentItemPort, schema)));
            RestHighLevelClient goodItemSearchEngineClient = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(goodItemHost, goodItemPort, schema)));
            RestHighLevelClient badItemSearchEngineClient = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(badItemHost, badItemPort, schema)));
            RestHighLevelClient rankingSystemClient = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(rankingHost, rankingPort, schema)));
            RestHighLevelClient summaryClient = new RestHighLevelClient(
                RestClient.builder(
                    new HttpHost(summaryHost, summaryPort, schema)));

            long start = System.currentTimeMillis();
            System.out.println("excellent_items loading...");
            DataLoader.loadIndex(excellentItemDataPath, excellentItemSearchEngineClient, "excellent_items", 0);
            System.out.println("excellent_items loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("good_items loading...");
            DataLoader.loadIndex(goodItemDataPath, goodItemSearchEngineClient, "good_items", 0);
            System.out.println("good_items loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("bad_items loading...");
            DataLoader.loadIndex(badItemDataPath, badItemSearchEngineClient, "bad_items", 0);
            System.out.println("bad_items loaded, cost " + cost(start) + " ms");

            start = System.currentTimeMillis();
            System.out.println("excellent_items items_ranking loading...");
            DataLoader.loadIndex(excellentItemDataPath, rankingSystemClient, "items_ranking", 1);
            System.out.println("excellent_items items_ranking loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("good_items items_ranking loading...");
            DataLoader.loadIndex(goodItemDataPath, rankingSystemClient, "items_ranking", 1);
            System.out.println("good_items items_ranking loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("bad_items items_ranking loading...");
            DataLoader.loadIndex(badItemDataPath, rankingSystemClient, "items_ranking", 1);
            System.out.println("bad_items items_ranking loaded, cost " + cost(start) + " ms");

            start = System.currentTimeMillis();
            System.out.println("excellent_items items_summary loading...");
            DataLoader.loadIndex(excellentItemDataPath, summaryClient, "items_summary", 2);
            System.out.println("excellent_items items_summary loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("good_items items_summary loading...");
            DataLoader.loadIndex(goodItemDataPath, summaryClient, "items_summary", 2);
            System.out.println("good_items items_summary loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("bad_items items_summary loading...");
            DataLoader.loadIndex(badItemDataPath, summaryClient, "items_summary", 2);
            System.out.println("bad_items items_summary loaded, cost " + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("neo4j user loading...");
            DataLoader.loadUserData(neo4jURI, userDataPath);
            System.out.println("neo4j user loaded, cost " + cost(start) + " ms");
            System.out.println("all data load finished!!");

            try {
                excellentItemSearchEngineClient.close();
                goodItemSearchEngineClient.close();
                badItemSearchEngineClient.close();
                rankingSystemClient.close();
                summaryClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @CommandLine.Command(name = "generate", description = "generate data")
    static class Generator implements Runnable {

        @CommandLine.Option(names = "--user", description = "user data output path", defaultValue = "user_data.txt")
        private String userDataOutputPath;

        @CommandLine.Option(names = "--excellent-item-dist", description = "excellent item distribution file",
            defaultValue = "excellent_item_segment.txt")
        private String excellentItemDistributionFilePath;

        @CommandLine.Option(names = "--good-item-dist", description = "good item distribution file",
            defaultValue = "good_item_segment.txt")
        private String goodItemDistributionFilePath;

        @CommandLine.Option(names = "--bad-item-dist", description = "bad item distribution file",
            defaultValue = "bad_item_segment.txt")
        private String badItemDistributionFilePath;

        @CommandLine.Option(names = "--excellent-item", description = "excellent item data output path",
            defaultValue = "excellent_item.txt")
        private String excellentItemDataOutputPath;

        @CommandLine.Option(names = "--good-item", description = "good item data output path",
            defaultValue = "good_item.txt")
        private String goodItemDataOutputPath;

        @CommandLine.Option(names = "--bad-item", description = "bad item data output path",
            defaultValue = "bad_item.txt")
        private String badItemDataOutputPath;

        @CommandLine.Option(names = "--model-params", description = "burr12 model params",
            defaultValue = "burr12_params_20.csv")
        private String burr12ParamsPath;

        @CommandLine.Option(names = "--scale", description = "data scale", defaultValue = "1")
        private int scale;

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            System.out.println("user data generating...");
            generateUserData(scale, userDataOutputPath);
            System.out.println("user data generated, time cost=" + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("EXCELLENT item generating...");
            generateItemData(scale, Item.EXCELLENT, excellentItemDistributionFilePath, excellentItemDataOutputPath);
            System.out.println("EXCELLENT item generated, time cost=" + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("GOOD item generating...");
            generateItemData(scale, Item.GOOD, goodItemDistributionFilePath, goodItemDataOutputPath);
            System.out.println("GOOD item generated, time cost=" + cost(start) + " ms");
            start = System.currentTimeMillis();
            System.out.println("BAD item generating...");
            generateItemData(scale, Item.BAD, badItemDistributionFilePath, badItemDataOutputPath);
            System.out.println("BAD item generated, time cost=" + cost(start) + " ms");
        }

    }

    private static long cost(long start) {
        return System.currentTimeMillis() - start;
    }

    @CommandLine.Command(name = "transform", description = "transform format of search log")
    static class Transformer implements Runnable {

        @CommandLine.Option(names = "--log", description = "search log path", required = true)
        private String searchLogPath;

        @CommandLine.Option(names = "--out", description = "output path of transformed log", required = true)
        private String transformedSearchLogPath;

        @Override
        public void run() {
            DataTransformUtils.transformSearchLogFormat(searchLogPath, transformedSearchLogPath);
        }
    }

    public static void main(String... args) {
        CommandLine.run(new Benchmark(), args);
    }
}
