package com.pms.log.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.pms.log.entity.ErrorLog;
import com.pms.log.entity.ErrorCountHistogram;
import com.pms.log.entity.AppUsage;
import com.pms.log.entity.Bucket;
import com.pms.log.service.IAppService;
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
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.LongBounds;
import org.elasticsearch.search.aggregations.metrics.ParsedValueCount;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

@Service
public class AppServiceImpl  implements IAppService{

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public AppUsage selectAppUsage(Integer from, Integer size, String startTime, String endTime, Integer interval) {

        //创建一个查询请求，并指定索引名称
        SearchRequest searchRequest = new SearchRequest("springboot-study-*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 日期区间
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));

        // 条件查询
        boolQuery.must(QueryBuilders.matchQuery("level", "ERROR"));
        searchSourceBuilder.query(boolQuery);

        // 封装
        AppUsage appUsage = new AppUsage();

        // Error计数 柱状图
        appUsage.setErrorCountHistogram(this.getErrorCountHistogram(startTime, endTime,
                interval,
                searchSourceBuilder,
                searchRequest));

        // Error message
        appUsage.setErrorsQuerryLog(this.getAppErrors(from, size, searchSourceBuilder, searchRequest));

        return appUsage;
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
                errorLog.setDate( date.toString(DatePattern.NORM_DATETIME_FORMAT));
                errorLog.setLevel((String) hits.get("level"));
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

}
