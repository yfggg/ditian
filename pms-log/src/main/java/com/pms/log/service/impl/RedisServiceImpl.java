package com.pms.log.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.pms.log.entity.*;
import com.pms.log.service.IRedisService;
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
import org.elasticsearch.search.aggregations.bucket.terms.ParsedTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedMax;
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
public class RedisServiceImpl implements IRedisService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public RedisUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval) {

        // 封装
        RedisUsage redisUsage = new RedisUsage();

        //创建一个查询请求，并指定索引名称
        SearchRequest searchRequest = new SearchRequest("filebeat-*");

        // 慢查询
        SearchSourceBuilder slowSearchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder slowBoolQuery = QueryBuilders.boolQuery();
        slowBoolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
        slowBoolQuery.must(QueryBuilders.matchQuery("event.dataset", "redis.slowlog"));
        slowSearchSourceBuilder.query(slowBoolQuery);

        // 慢查询最高时间排名
        redisUsage.setTopSlowestCommands(this.getTopSlowestCommands(slowSearchSourceBuilder, searchRequest));

        // 慢查询 message
        redisUsage.setSlowLogs(this.getSlowLogs(from, size, slowSearchSourceBuilder, searchRequest));

        // 运行日志查询
        SearchSourceBuilder logSearchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder logBoolQuery = QueryBuilders.boolQuery();
        logBoolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
        logBoolQuery.must(QueryBuilders.matchQuery("event.module", "redis"));
        logBoolQuery.must(QueryBuilders.matchQuery("fileset.name", "log"));
        logSearchSourceBuilder.query(logBoolQuery);

        // log message
        redisUsage.setLogs(this.getLogs(from, size, logSearchSourceBuilder, searchRequest));

        // log 计数柱状图
        redisUsage.setLevelCountHistogram(this.getLevelCountHistogram(startTime, endTime,
                interval,
                logSearchSourceBuilder,
                searchRequest));

        return redisUsage;
    }

//    -----------------------------------------------------------------------------------------

    private List<TopSlowestCommand> getTopSlowestCommands(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 打组 降序 限定条数
        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("cmd_group").field("redis.slowlog.cmd")
                        .subAggregation(AggregationBuilders.max("duration_max").field("redis.slowlog.duration.us"))
                        .order(BucketOrder.aggregation("duration_max", false))
                        .size(5)
        );

        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        List<TopSlowestCommand> topSlowestCommands = new ArrayList<>();

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            Aggregations aggregations = response.getAggregations();
            ParsedStringTerms cmdGroup = (ParsedStringTerms) aggregations.asMap().get("cmd_group");

            for (Terms.Bucket entry : cmdGroup.getBuckets()) {
                TopSlowestCommand slowestCommand = new TopSlowestCommand();
                slowestCommand.setCommand(entry.getKey().toString());
                ParsedMax max = (ParsedMax) entry.getAggregations().asMap().get("duration_max");
                String duration = new BigDecimal(Double.valueOf(max.getValue()))
                        .setScale(1, RoundingMode.HALF_UP).toPlainString();
                slowestCommand.setDuration(duration);
                topSlowestCommands.add(slowestCommand);
            }

            return topSlowestCommands;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<RedisSlowLog> getSlowLogs(Integer from, Integer size,
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

            List<RedisSlowLog> slowLogs = new ArrayList<>();

            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                Map hits = JSON.parseObject(hitJson);
                RedisSlowLog slowLog = new RedisSlowLog();
                DateTime date = DateUtil.parseUTC((String) hits.get("@timestamp"));
                slowLog.setTime(date.toString(DatePattern.NORM_DATETIME_FORMAT));
                Map host = (Map) hits.get("host");
                slowLog.setHostName(host.get("name").toString());
                slowLog.setMessage((String) hits.get("message"));
                Map redis = (Map) hits.get("redis");
                Map slowlog = (Map) redis.get("slowlog");
                Map duration = (Map) slowlog.get("duration");
                slowLog.setSlowlogDurationUs((Integer) duration.get("us"));
                slowLog.setSlowlogKey((String) slowlog.get("key"));
                slowLogs.add(slowLog);
            }
            return slowLogs;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<RedisLog> getLogs(Integer from, Integer size,
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

            List<RedisLog> logs = new ArrayList<>();

            for (SearchHit searchHit : searchHits) {
                String hitJson = searchHit.getSourceAsString();
                Map hits = JSON.parseObject(hitJson);
                RedisLog log = new RedisLog();
                DateTime date = DateUtil.parseUTC((String) hits.get("@timestamp"));
                log.setTime(date.toString(DatePattern.NORM_DATETIME_FORMAT));
                Map host = (Map) hits.get("host");
                log.setHostName(host.get("name").toString());
                log.setMessage((String) hits.get("message"));
                Map log1 = (Map) hits.get("log");
                log.setLevel((String) log1.get("level"));
                Map redis = (Map) hits.get("redis");
                Map redislog = (Map) redis.get("log");
                log.setRole((String) redislog.get("role"));
                logs.add(log);
            }
            return logs;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private RedisLogsLevelCountHistogram getLevelCountHistogram(String startTime, String endTime,
                                                       Integer interval,
                                                       SearchSourceBuilder searchSourceBuilder,
                                                       SearchRequest searchRequest) {


        startTime = DateUtil.parseUTC(startTime+":59.000Z").toString();
        endTime =DateUtil.parseUTC(endTime+":59.000Z").toString();
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh")
                .subAggregation(
                        AggregationBuilders.terms("level_group").field("log.level")
                )
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
            return levelCountHistogramForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private RedisLogsLevelCountHistogram levelCountHistogramForEach(List<? extends Histogram.Bucket> buckets) {

        // 封装
        RedisLogsLevelCountHistogram levelCountHistogram = new RedisLogsLevelCountHistogram();
        Bucket verbose = new Bucket();
        List<String> verboseDates = new ArrayList<>();
        List<String> verboseValues = new ArrayList<>();

        Bucket debug = new Bucket();
        List<String> debugDates = new ArrayList<>();
        List<String> debugValues = new ArrayList<>();

        Bucket warning = new Bucket();
        List<String> warningDates = new ArrayList<>();
        List<String> warningValues = new ArrayList<>();

        Bucket notice = new Bucket();
        List<String> noticeDates = new ArrayList<>();
        List<String> noticeValues = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();
            ParsedStringTerms terms = bucket.getAggregations().get("level_group");

            if(terms.getBuckets().size() > 0) {
                terms.getBuckets().stream()
                        .filter(b -> b.getKey().toString().equals("verbose"))
                        .forEach(b -> verboseValues.add(String.valueOf(b.getDocCount())));
                terms.getBuckets().stream()
                        .filter(b -> b.getKey().toString().equals("debug"))
                        .forEach(b -> debugValues.add(String.valueOf(b.getDocCount())));
                terms.getBuckets().stream()
                        .filter(b -> b.getKey().toString().equals("warning"))
                        .forEach(b -> warningValues.add(String.valueOf(b.getDocCount())));
                terms.getBuckets().stream()
                        .filter(b -> b.getKey().toString().equals("notice"))
                        .forEach(b -> noticeValues.add(String.valueOf(b.getDocCount())));
            } else {
                verboseValues.add("0");
                debugValues.add("0");
                warningValues.add("0");
                noticeValues.add("0");
            }

            verboseDates.add(date);
            verbose.setDates(verboseDates);
            verbose.setValues(verboseValues);

            debugDates.add(date);
            debug.setDates(debugDates);
            debug.setValues(debugValues);

            warningDates.add(date);
            warning.setDates(warningDates);
            warning.setValues(warningValues);

            noticeDates.add(date);
            notice.setDates(noticeDates);
            notice.setValues(noticeValues);
        }

        // 装箱
        levelCountHistogram.setVerbose(verbose);
        levelCountHistogram.setDebug(debug);
        levelCountHistogram.setNotice(notice);
        levelCountHistogram.setWarning(warning);

        return levelCountHistogram;
    }

}
