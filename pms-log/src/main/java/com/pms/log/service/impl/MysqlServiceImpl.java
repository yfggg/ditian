package com.pms.log.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.pms.log.entity.*;
import com.pms.log.service.IAppService;
import com.pms.log.service.IMysqlService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.LongBounds;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class MysqlServiceImpl implements IMysqlService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public MysqlUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval) {

        // 封装
        MysqlUsage mysqlUsage = new MysqlUsage();

        //创建一个查询请求，并指定索引名称
        SearchRequest searchRequest = new SearchRequest("filebeat-*");

        // 慢查询
        SearchSourceBuilder slowSearchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder slowBoolQuery = QueryBuilders.boolQuery();
        slowBoolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
        slowBoolQuery.must(QueryBuilders.matchQuery("event.module", "mysql"));
        slowBoolQuery.must(QueryBuilders.matchQuery("fileset.name", "slowlog"));
        slowSearchSourceBuilder.query(slowBoolQuery);

        // 慢查询计数柱状图
        mysqlUsage.setSlowQueriesHistogram(this.getSlowQueriesHistogram(startTime, endTime,
                interval,
                slowSearchSourceBuilder,
                searchRequest));

        // 慢查询 message
        mysqlUsage.setTopSlowQueries(this.getTopSlowQueries(slowSearchSourceBuilder, searchRequest));

        // 运行错误查询
        SearchSourceBuilder errSearchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder errBoolQuery = QueryBuilders.boolQuery();
        errBoolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
        errBoolQuery.must(QueryBuilders.matchQuery("event.module", "mysql"));
        errBoolQuery.must(QueryBuilders.matchQuery("fileset.name", "error"));
        errSearchSourceBuilder.query(errBoolQuery);

        // err message
        mysqlUsage.setErrorLogs(this.getAppErrors(from, size, errSearchSourceBuilder, searchRequest));

        // err计数柱状图
        mysqlUsage.setErrorLogsOverTime(this.getErrorCountHistogram(startTime, endTime,
                interval,
                errSearchSourceBuilder,
                searchRequest));

        return mysqlUsage;
    }

//    -----------------------------------------------------------------------------------------

    private List<ErrorLog> getAppErrors(Integer from, Integer size,
                                        SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 降序和分页
        searchSourceBuilder.sort("@timestamp", SortOrder.DESC);
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);

        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] searchHits = response.getHits().getHits();

            List<ErrorLog> errorLogs = new ArrayList<>();

            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                Map hits = JSON.parseObject(hitJson);
                ErrorLog errorLog = new ErrorLog();
                DateTime date = DateUtil.parseUTC((String) hits.get("@timestamp"));
                errorLog.setDate(date.toString(DatePattern.NORM_DATETIME_FORMAT));
                Map log = (Map) hits.get("log");
                errorLog.setLevel(log.get("level").toString());
                errorLog.setMessage((String) hits.get("message"));
                errorLogs.add(errorLog);
            }
            return errorLogs;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ErrorCountHistogram getErrorCountHistogram(String startTime, String endTime,
                                                       Integer interval,
                                                       SearchSourceBuilder searchSourceBuilder,
                                                       SearchRequest searchRequest) {


        startTime = DateUtil.parseUTC(startTime+":59.000Z").toString();
        endTime =DateUtil.parseUTC(endTime+":59.000Z").toString();
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh")
                .field("@timestamp")
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss")
                .extendedBounds(new LongBounds(DateUtil.parse(startTime).getTime(), DateUtil.parse(endTime).getTime()));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh");

            // 获取桶聚合结果
            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            return errorCountHistogramForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ErrorCountHistogram errorCountHistogramForEach(List<? extends Histogram.Bucket> buckets) {

        // 封装
        ErrorCountHistogram errorCountHistogram = new ErrorCountHistogram();
        Bucket errorCount = new Bucket();
        List<String> errorCountDates = new ArrayList<>();
        List<String> errorCountValues = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();
            errorCountDates.add(date);
            errorCountValues.add(String.valueOf(bucket.getDocCount()));
            errorCount.setDates(errorCountDates);
            errorCount.setValues(errorCountValues);
        }

        // 装箱
        errorCountHistogram.setErrorCount(errorCount);

        return errorCountHistogram;
    }

    private List<TopSlowestQueries> getTopSlowQueries(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 打组 降序 限定条数
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("query_group").field("mysql.slowlog.query")
                        .subAggregation(AggregationBuilders.terms("user_group").field("user.name"))
                        .subAggregation(AggregationBuilders.max("query_time").field("event.duration"))
                        .order(BucketOrder.aggregation("query_time", false))
                        .size(5)
        );

        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        List<TopSlowestQueries> topSlowestQueries = new ArrayList<>();

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            ParsedStringTerms queryGroup = (ParsedStringTerms) aggregations.asMap().get("query_group");

            for (Terms.Bucket entry : queryGroup.getBuckets()) {
                TopSlowestQueries slow = new TopSlowestQueries();
                slow.setQuery(entry.getKey().toString());
                ParsedStringTerms userGroup = (ParsedStringTerms) entry.getAggregations().asMap().get("user_group");
                slow.setUser(userGroup.getBuckets().stream().findFirst().get().getKey().toString());
                ParsedMax max = (ParsedMax) entry.getAggregations().asMap().get("query_time");
                String queryTime = new BigDecimal(Double.valueOf(max.getValue()))
                        .divide(new BigDecimal(1000000))
                        .setScale(1, RoundingMode.HALF_UP).toPlainString();
                slow.setQueryTime(queryTime);
                topSlowestQueries.add(slow);
            }

            return topSlowestQueries;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SlowQueriesCountHistogram getSlowQueriesHistogram(String startTime, String endTime,
                                                          Integer interval,
                                                          SearchSourceBuilder searchSourceBuilder,
                                                          SearchRequest searchRequest) {


        startTime = DateUtil.parseUTC(startTime+":59.000Z").toString();
        endTime =DateUtil.parseUTC(endTime+":59.000Z").toString();
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh")
                .field("@timestamp")
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss")
                .extendedBounds(new LongBounds(DateUtil.parse(startTime).getTime(), DateUtil.parse(endTime).getTime()));

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.count("slow_count").field("fileset.name"));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh");

            // 获取桶聚合结果
            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            return slowQueriesForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SlowQueriesCountHistogram slowQueriesForEach(List<? extends Histogram.Bucket> buckets) {

        // 封装
        SlowQueriesCountHistogram slowQueriesCountHistogram = new SlowQueriesCountHistogram();
        Bucket errorCount = new Bucket();
        List<String> slowDates = new ArrayList<>();
        List<String> slowValues = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();
            ParsedValueCount valueCount = (ParsedValueCount) bucket.getAggregations().asMap().get("slow_count");
            slowDates.add(date);
            slowValues.add(valueCount.getValueAsString());
            errorCount.setDates(slowDates);
            errorCount.setValues(slowValues);
        }

        // 装箱
        slowQueriesCountHistogram.setSlowQueriesCount(errorCount);

        return slowQueriesCountHistogram;
    }

}
