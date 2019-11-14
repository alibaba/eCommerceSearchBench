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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import com.alibaba.benchmark.stat.StatService;

@RestController
public class SearchController {
    final Logger log = LoggerFactory.getLogger(SearchController.class);

    private RestHighLevelClient excellentItemSearchClient;
    private RestHighLevelClient goodItemSearchClient;
    private RestHighLevelClient badItemSearchClient;
    private RestHighLevelClient rankingSystemClient;
    private RestHighLevelClient summarySystemClient;
    private final int FETCH_SIZE = 1000;
    private final int PAGE_SIZE = 10;
    private final String FIELD = "title";

    @Value("${excellent_item_index}")
    private String excellentItemsIndex;
    @Value("${good_item_index}")
    private String goodItemsIndex;
    @Value("${bad_item_index}")
    private String badItemsIndex;
    @Value("${ranking_index}")
    private String rankingIndex;
    @Value("${summary_index}")
    private String summaryIndex;
    @Value("${qp.url}")
    private String qpURL;

    @Value("${excellent.item.host}")
    private String excellentItemHost;
    @Value("${excellent.item.port}")
    private Integer excellentItemPort;

    @Value("${good.item.host}")
    private String goodItemHost;
    @Value("${good.item.port}")
    private Integer goodItemPort;

    @Value("${bad.item.host}")
    private String badItemHost;
    @Value("${bad.item.port}")
    private Integer badItemPort;

    @Value("${ranking.system.host}")
    private String rankingSystemHost;
    @Value("${ranking.system.port}")
    private Integer rankingSystemPort;

    @Value("${summary.system.host}")
    private String summarySystemHost;
    @Value("${summary.system.port}")
    private Integer summarySystemPort;

    private StatService searchStatService = new StatService();

    private StatService ha3SearchStatService = new StatService(true);

    final String schema = "http";

    @PostConstruct
    public void init() {
        this.excellentItemSearchClient = new SearchClient(excellentItemHost, excellentItemPort, schema).getClient();
        this.goodItemSearchClient = new SearchClient(goodItemHost, goodItemPort, schema).getClient();
        this.badItemSearchClient = new SearchClient(badItemHost, badItemPort, schema).getClient();
        this.rankingSystemClient = new SearchClient(rankingSystemHost, rankingSystemPort, schema).getClient();
        this.summarySystemClient = new SearchClient(summarySystemHost, summarySystemPort, schema).getClient();
    }

    @RequestMapping("/checkpreload.htm")
    public String health() {
        return "success";
    }

    @RequestMapping("/")
    String welcome() {
        return "Welcome!";
    }

    @PostMapping("/test")
    String getMyId(@RequestBody QueryInfo queryInfo) {
        log.info(String.valueOf(queryInfo));
        return String.valueOf(new Random().nextLong());
    }

    @PostMapping("/search")
    List<GetResponse> search(@RequestBody QueryInfo queryInfo) throws IOException {
        long totalSt = System.currentTimeMillis();
        long st = totalSt;
        List<Double> weights = queryPlanner(queryInfo.getUid(), queryInfo.getQuery());
        long queryPlannerTime = System.currentTimeMillis() - st;
        st = System.currentTimeMillis();
        List<String> docIds = matchPhase(queryInfo.getQuery(), queryInfo.getFetchSize());
        long matchPhaseTime = System.currentTimeMillis() - st;
        st = System.currentTimeMillis();
        docIds = rankingPhase(docIds, rankingIndex, queryInfo.getQuery(), weights);
        long rankingPhaseTime = System.currentTimeMillis() - st;
        st = System.currentTimeMillis();
        final int pageSize = 6;
        if (docIds.size() > pageSize) {
            docIds = docIds.subList(0, pageSize);
        }
        List<GetResponse> responses = fetchPhase(summaryIndex, docIds);
        long fetchPhaseTime = System.currentTimeMillis() - st;
        long totalCost = System.currentTimeMillis() - totalSt;
        String out = String.format("queryPlannerTime=%d, matchPhaseTime=%d, "
                + "rankingPhaseTime=%d fetchPhaseTime=%d, fetchedResult=%d",
            queryPlannerTime, matchPhaseTime, rankingPhaseTime, fetchPhaseTime, docIds.size());
        searchStatService.accept(totalCost, queryPlannerTime, matchPhaseTime, rankingPhaseTime, fetchPhaseTime);
        //System.out.println(out);
        log.info(out);
        return responses;
    }

    /**
     * query HA3 only
     */
    @PostMapping("/searchHa3")
    List<GetResponse> searchHa3(@RequestBody QueryInfo queryInfo) throws IOException {
        long st = System.currentTimeMillis();
        List<String> docIds = matchPhase(queryInfo.getQuery(), queryInfo.getFetchSize());
        long matchPhaseTime = System.currentTimeMillis() - st;
        String out = String.format("queryPlannerTime=, matchPhaseTime=%d, fetchedResult=%d",
            matchPhaseTime, docIds.size());
        ha3SearchStatService.acceptHa3Searcher(matchPhaseTime, matchPhaseTime);
        log.info(out);
        return new ArrayList<>();
    }

    /**
     * 获取用户个性化信息。
     *
     * @param uid
     * @param query
     * @return
     */
    private List<Double> queryPlanner(String uid, String query) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<QueryInfo> request = new HttpEntity<>(new QueryInfo(uid, query));
        Weights weights = restTemplate.postForObject(qpURL, request, Weights.class);

        return Objects.requireNonNull(weights).getWeights();
    }

    private List<String> matchPhase(String query, int size) throws IOException {
        long st = System.currentTimeMillis();
        SearchRequest request = buildRequest(excellentItemsIndex, FIELD, query, size);
        List<String> docIds = new ArrayList<>(size);
        SearchResponse response = excellentItemSearchClient.search(request, RequestOptions.DEFAULT);
        addToDocs(docIds, response);
        long excellentTime = System.currentTimeMillis() - st;
        long goodTime = -1;
        long badTime = -1;

        if (docIds.size() < size) {
            st = System.currentTimeMillis();
            request = buildRequest(goodItemsIndex, FIELD, query, size - docIds.size());
            response = goodItemSearchClient.search(request, RequestOptions.DEFAULT);
            addToDocs(docIds, response);
            goodTime = System.currentTimeMillis() - st;

            if (docIds.size() < size) {
                st = System.currentTimeMillis();
                request = buildRequest(badItemsIndex, FIELD, query, size - docIds.size());
                response = badItemSearchClient.search(request, RequestOptions.DEFAULT);
                addToDocs(docIds, response);
                badTime = System.currentTimeMillis() - st;
            }
        }
        //log.info("matchPhase: " + docIds.toString());
        log.info("excellentTime={}, goodTime={}, badTime={}, docIds.size={}",
            excellentTime, goodTime, badTime, docIds.size());

        return docIds;
    }

    private SearchRequest buildRequest(String index, String field, String query, int fetchSize) {
        SearchRequest request = new SearchRequest(index);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.fetchSource(false);
        builder.query(new MatchQueryBuilder(field, query));
        builder.from(0);
        builder.size(fetchSize);
        request.source(builder);
        return request;
    }

    private void addToDocs(List<String> docIds, SearchResponse response) {
        for (SearchHit hit : response.getHits().getHits()) {
            docIds.add(hit.getId());
        }
    }

    private List<String> rankingPhase(List<String> docIds, String index, String query, List<Double> weights)
        throws IOException {

        //        String queryTemplate = "{\"_source\":false,
        // \"query\":{\"bool\":{\"filter\":{\"terms\":{\"_id\":%s}}}},
        // \"rescore\":{\"query\":{\"rescore_query\":{\"sltr\":{\"params\":{\"keywords\":\"%s\"},
        // \"model\":\"test_6\"}}}}}";
        //        String queryTemplate = "{\"_source\":false,
        // \"query\":{\"bool\":{\"filter\":{\"terms\":{\"_id\":%s}}}},
        // \"rescore\":{\"query\":{\"rescore_query\":{\"function_score\":{\"query\":{\"match\":{\"title\":\"%s\"}},
        // \"functions\":[{\"field_value_factor\":{\"field\":\"price\",\"factor\":%s}},
        // {\"field_value_factor\":{\"field\":\"ratesum\",\"factor\":%s}}],\"boost\":%s,\"score_mode\":\"sum\",
        // \"boost_mode\":\"sum\"}}}}}";
        String queryTemplate
            = "{\"_source\":false,\"query\":{\"bool\":{\"filter\":{\"terms\":{\"_id\":%s}}}},"
            + "\"rescore\":{\"window_size\":%d,\"query\":{\"rescore_query\":{\"function_score\":{\"query\":{\"match"
            + "\":{\"title\":\"%s\"}},\"functions\":[{\"field_value_factor\":{\"field\":\"price\",\"factor\":%s}},"
            + "{\"field_value_factor\":{\"field\":\"ratesum\",\"factor\":%s}}],\"boost\":%s,\"score_mode\":\"sum\","
            + "\"boost_mode\":\"sum\"}}}}}";
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String idsString = ow.writeValueAsString(docIds);
        //        String queryJsonString = String.format(
        //                queryTemplate,
        //                idsString, query,
        //                String.valueOf(weights.get(1)),
        //                String.valueOf(weights.get(2)),
        //                String.valueOf(weights.get(0)));

        String queryJsonString = String.format(
            queryTemplate,
            idsString, FETCH_SIZE, query,
            String.valueOf(weights.get(1)),
            String.valueOf(weights.get(2)),
            String.valueOf(weights.get(0)));
        //log.info(queryJsonString);

        Request request = new Request("GET", "/" + index + "/_search");
        request.setJsonEntity(queryJsonString);
        //log.info("queryJsonString={}", queryJsonString);
        Response response = rankingSystemClient.getLowLevelClient().performRequest(request);

        String responseJson = EntityUtils.toString(response.getEntity());

        Pattern pattern = Pattern.compile("_id\":\"(.*?)\",");
        Matcher matcher = pattern.matcher(responseJson);

        List<String> rescoreDocIds = new ArrayList<>();
        while (matcher.find()) {
            rescoreDocIds.add(matcher.group(1));
        }

        //log.info("rankingPhase: " + rescoreDocIds.toString());

        return rescoreDocIds.subList(0, Math.min(PAGE_SIZE, rescoreDocIds.size()));
    }

    private List<GetResponse> fetchPhase(String index, List<String> docIds) throws IOException {
        List<GetResponse> responses = new ArrayList<>();
        if (docIds.isEmpty()) {
            return responses;
        }

        MultiGetRequest request = new MultiGetRequest();
        docIds.forEach(id -> request.add(index, "_doc", id));
        MultiGetResponse response = summarySystemClient.mget(request, RequestOptions.DEFAULT);

        for (MultiGetItemResponse multiGetItemResponse : response) {
            //            log.info(String.valueOf(multiGetItemResponse.getResponse()));
            responses.add(multiGetItemResponse.getResponse());
        }

        return responses;
    }
}
