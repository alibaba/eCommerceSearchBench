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

import lombok.Data;

import java.io.Serializable;

public class QueryInfo implements Serializable {
    private String uid;
    private int page;
    private String query;
    /** 粗排阶段查询的接过数 */
    private int fetchSize = 1000;

    public QueryInfo(String uid, String query) {
        this.uid = uid;
        this.page = 1;
        this.query = query;
    }

    public QueryInfo(String uid, int page, String query) {
        this.uid = uid;
        this.page = page;
        this.query = query;
    }

    public QueryInfo() {
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getFetchSize() {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }
}
