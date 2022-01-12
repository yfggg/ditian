package com.pms.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.pms.system.entity.*;
import com.pms.system.service.ISystemService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.*;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.ParsedCardinality;
import org.elasticsearch.search.aggregations.pipeline.DerivativePipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.pipeline.ParsedDerivative;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author yang fan
 * @since 2021-12-13
 */
@Service
public class SystemServiceImpl implements ISystemService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SystemUsage selectTotalUsage(String startTime, String endTime, Integer interval) {
        //创建一个查询请求，并指定索引名称
        SearchRequest searchRequest = new SearchRequest("metricbeat-*");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        // 日期区间
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolQuery.must(QueryBuilders.rangeQuery("@timestamp")
                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
        searchSourceBuilder.query(boolQuery);

        // 封装
        SystemUsage systemUsage = new SystemUsage();

        // MemoryHistogram 柱状图
        systemUsage.setMemoryHistogramUsage(this.getMemoryHistogram(30, searchSourceBuilder, searchRequest));
        // loadHistogram 柱状图
        systemUsage.setSystemLoadHistogramUsage(this.getSystemLoadHistogram(30, searchSourceBuilder, searchRequest));
        // CpuHistogram 柱状图
        systemUsage.setCpuHistogramUsage(this.getCpuHistogram(30, searchSourceBuilder, searchRequest));
        // disk 百分比
        systemUsage.setDiskPctUsage(this.getDiskPct(searchSourceBuilder, searchRequest)+"%");
        // memory 百分比
        systemUsage.setMemoryPctUsage(this.getMemoryPct(searchSourceBuilder, searchRequest)+"%");
        // cpu 百分比
        systemUsage.setCpuPctUsage(this.getCpuPct(searchSourceBuilder, searchRequest)+"%");
        // 内存总数
        systemUsage.setMemoryTotalUsage(this.getMemoryUsed(searchSourceBuilder, searchRequest));
        // 内存使用数
        systemUsage.setMemoryUsedUsage(this.getMemoryTotal(searchSourceBuilder, searchRequest));
        // 进程数
        systemUsage.setProcessesUsage(this.getProcesses(searchSourceBuilder, searchRequest));

        // 条件查询 (这段代码位置不要动) 上面不需要这些条件
        boolQuery.must(QueryBuilders.matchQuery("system.network.name", "ens33"));
//        boolQuery.should(QueryBuilders.matchQuery("system.network.name", "virbr0"));
//        boolQuery.should(QueryBuilders.matchQuery("system.network.name", "virbr0-nic"));
        searchSourceBuilder.query(boolQuery);

        // 出入站流量 柱状图
        NetworkTrafficHistogram networkTrafficHistogram
                = this.getNetworkTrafficHistogram(30, searchSourceBuilder, searchRequest);
        systemUsage.setNetworkTrafficHistogramUsage(networkTrafficHistogram);
        InboundAndOutbound inboundAndOutbound = this.getInboundAndOutbound(30, searchSourceBuilder, searchRequest);
        // 入站 TotalTransferred
        systemUsage.setInTotalTransferred(inboundAndOutbound.getInTotalTransferred());
        // 出站 TotalTransferred
        systemUsage.setOutTotalTransferred(inboundAndOutbound.getOutTotalTransferred());
        // Inbound Traffic
        List<String> inValues = networkTrafficHistogram.getInbound().getValues();
        systemUsage.setInboundTraffic(networkTrafficHistogram.getInbound().getValues().get(inValues.size()-1)+"KB/S");
        // Outbound Traffic
        List<String> outValues = networkTrafficHistogram.getOutbound().getValues();
        systemUsage.setOutboundTraffic(networkTrafficHistogram.getOutbound().getValues().get(outValues.size()-1)+"KB/S");

        return systemUsage;
    }

/*    -----------------------------------------------------------------------------------------------*/
//    单个仪表板

    private InboundAndOutbound getInboundAndOutbound(Integer interval, SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh12")
                .field("@timestamp")
//                .calendarInterval(DateHistogramInterval.HOUR)
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss");

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max("network_in_max").field("system.network.in.bytes"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max("network_out_max").field("system.network.out.bytes"));
        dateHistogramAggregationBuilder.subAggregation(PipelineAggregatorBuilders.derivative("dein","network_in_max")
                .unit(DateHistogramInterval.minutes(interval)));
        dateHistogramAggregationBuilder.subAggregation(PipelineAggregatorBuilders.derivative("deout","network_out_max")
                .unit(DateHistogramInterval.minutes(interval)));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        InboundAndOutbound inboundAndOutbound = new InboundAndOutbound();

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh12");

            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            // 循环遍历各个桶结果
            BigDecimal inTotal = BigDecimal.ZERO;
            BigDecimal outTotal = BigDecimal.ZERO;

            for (Histogram.Bucket bucket : buckets) {

                if(null != bucket.getAggregations().asMap().get("dein")) {
                    ParsedDerivative derivative = (ParsedDerivative) bucket.getAggregations().asMap().get("dein");
                    inTotal = inTotal.add(new BigDecimal(Double.valueOf(derivative.normalizedValue())));
                }

                if(null != bucket.getAggregations().asMap().get("deout")) {
                    ParsedDerivative derivative = (ParsedDerivative) bucket.getAggregations().asMap().get("deout");
                    outTotal = outTotal.add(new BigDecimal(Double.valueOf(derivative.normalizedValue())));
                }
            }

            inboundAndOutbound.setInTotalTransferred(this.conver(inTotal.longValue()));
            inboundAndOutbound.setOutTotalTransferred(this.conver(outTotal.longValue()));

            return inboundAndOutbound;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getProcesses(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.cardinality("process_count").field("process.pid"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            ParsedCardinality cardinality = (ParsedCardinality) aggregations.getAsMap().get("process_count");
            return cardinality.getValueAsString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getMemoryTotal(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.avg("total_used_avg").field("system.memory.total"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();

//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("actual_used_avg")));

            BigDecimal total = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap()
                    .get("total_used_avg"))).get("value");
            return conver(total.longValue());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getMemoryUsed(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.avg("actual_used_avg").field("system.memory.actual.used.bytes"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();

//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("actual_used_avg")));

            BigDecimal used = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("actual_used_avg"))).get("value");
            return conver(used.longValue());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getCpuPct(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.avg("user_avg").field("system.cpu.user.pct"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("system_avg").field("system.cpu.system.pct"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("cores_avg").field("system.cpu.cores"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();

//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("user_avg")));
//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("system_avg")));
//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("cores_avg")));

            BigDecimal user = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("user_avg"))).get("value");
            BigDecimal system = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("system_avg"))).get("value");
            Integer n = (Integer) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("cores_avg"))).get("value");

            if(n > 0) {
                return user.add(system)
                        .divide(new BigDecimal(n))
                        .multiply(new BigDecimal(100))
                        .setScale(3, RoundingMode.HALF_UP)
                        .toString();
            } else {
                return "";
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getDiskPct(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.avg("used_avg").field("system.fsstat.total_size.used"));
        searchSourceBuilder.aggregation(AggregationBuilders.avg("total_avg").field("system.fsstat.total_size.total"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();

//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("used_avg")));
//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("total_avg")));

            BigDecimal used = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("used_avg"))).get("value");
            BigDecimal total = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("total_avg"))).get("value");
            return used.divide(total, 3, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100))
                    .toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getMemoryPct(SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 聚合查询
        searchSourceBuilder.aggregation(AggregationBuilders.avg("memory_avg").field("system.memory.actual.used.pct"));
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();

//            System.out.println(JSONUtil.toJsonStr(aggregations.getAsMap().get("memory_avg")));

            BigDecimal memory = (BigDecimal) JSON.parseObject(JSONUtil.toJsonStr(aggregations.getAsMap().get("memory_avg"))).get("value");
            return memory.multiply(new BigDecimal(100))
                    .setScale(3, RoundingMode.HALF_UP)
                    .toString();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

/*    -----------------------------------------------------------------------------------------------*/
//    直方图

    private NetworkTrafficHistogram getNetworkTrafficHistogram(Integer interval, SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {

        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh11")
                .field("@timestamp")
//                .calendarInterval(DateHistogramInterval.HOUR)
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss");

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max("network_in_max").field("system.network.in.bytes"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.max("network_out_max").field("system.network.out.bytes"));
        dateHistogramAggregationBuilder.subAggregation(PipelineAggregatorBuilders.derivative("dein","network_in_max")
                .unit(DateHistogramInterval.SECOND));
        dateHistogramAggregationBuilder.subAggregation(PipelineAggregatorBuilders.derivative("deout","network_out_max")
                .unit(DateHistogramInterval.SECOND));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh11");

            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            return NetworkTrafficForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private NetworkTrafficHistogram NetworkTrafficForEach(List<? extends Histogram.Bucket> buckets) {
        // 封装
        NetworkTrafficHistogram networkTrafficHistogram = new NetworkTrafficHistogram();
        Bucket inbound = new Bucket();
        List<String> inboundDates = new ArrayList<>();
        List<String> inboundValues = new ArrayList<>();

        Bucket outbound = new Bucket();
        List<String> outboundDates = new ArrayList<>();
        List<String> outboundValues = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {

            if(null != bucket.getAggregations().asMap().get("dein")) {
                inboundDates.add(bucket.getKeyAsString());
                ParsedDerivative derivative = (ParsedDerivative) bucket.getAggregations().asMap().get("dein");
                inboundValues.add(this.converKBS(Double.valueOf(derivative.normalizedValue()).longValue()));
//                System.out.println(bucket.getKeyAsString() + "------------" +
//                        this.converKBS(Double.valueOf(derivative.normalizedValue()).longValue()));
                inbound.setDates(inboundDates);
                inbound.setValues(inboundValues);
            }

            if(null != bucket.getAggregations().asMap().get("deout")) {
                outboundDates.add(bucket.getKeyAsString());
                ParsedDerivative derivative = (ParsedDerivative) bucket.getAggregations().asMap().get("deout");
                outboundValues.add(this.converKBS(Double.valueOf(derivative.normalizedValue()).longValue()));
//                System.out.println(bucket.getKeyAsString() + "------------" +
//                        this.converKBS(Double.valueOf(derivative.normalizedValue()).longValue()));
                outbound.setDates(outboundDates);
                outbound.setValues(outboundValues);
            }
        }

        // 装箱
        networkTrafficHistogram.setInbound(inbound);
        networkTrafficHistogram.setOutbound(outbound);

        return networkTrafficHistogram;
    }

    private CpuHistogram getCpuHistogram(Integer interval, SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh")
                .field("@timestamp")
//                .calendarInterval(DateHistogramInterval.HOUR)
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss");

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("user_avg").field("system.cpu.user.pct"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("system_avg").field("system.cpu.system.pct"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("nice_avg").field("system.cpu.nice.pct"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("irq_avg").field("system.cpu.irq.pct"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("softirq_avg").field("system.cpu.softirq.pct"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("iowait_avg").field("system.cpu.iowait.pct"));

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
            return cpuHistogramForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private CpuHistogram cpuHistogramForEach(List<? extends Histogram.Bucket> buckets) {
        // 封装
        CpuHistogram cpuHistogramUsage = new CpuHistogram();
        Bucket cpuUser = new Bucket();
        List<String> cpuUserDates = new ArrayList<>();
        List<String> cpuUserValues = new ArrayList<>();

        Bucket cpuSystem = new Bucket();
        List<String> cpuSystemDates = new ArrayList<>();
        List<String> cpuSystemValues = new ArrayList<>();

        Bucket cpuNice = new Bucket();
        List<String> cpuNiceDates = new ArrayList<>();
        List<String> cpuNiceValues = new ArrayList<>();

        Bucket cpuIrq = new Bucket();
        List<String> cpuIrqDates = new ArrayList<>();
        List<String> cpuIrqValues = new ArrayList<>();

        Bucket cpuSoftirq = new Bucket();
        List<String> cpuSoftirqDates = new ArrayList<>();
        List<String> cpuSoftirqValues = new ArrayList<>();

        Bucket cpuIowait = new Bucket();
        List<String> cpuIowaitDates = new ArrayList<>();
        List<String> cpuIowaitValues = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();

            Map<String, Object> userAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("user_avg")));
//            System.out.println(date+"-------------"+value);
            cpuUserDates.add(date);
            cpuUserValues.add(this.getValue(userAvg));
            cpuUser.setDates(cpuUserDates);
            cpuUser.setValues(cpuUserValues);

            Map<String, Object> systemAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("system_avg")));
//            System.out.println(date+"-------------"+value);
            cpuSystemDates.add(date);
            cpuSystemValues.add(this.getValue(systemAvg));
            cpuSystem.setDates(cpuSystemDates);
            cpuSystem.setValues(cpuSystemValues);

            Map<String, Object> niceAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("nice_avg")));
//            System.out.println(date+"-------------"+value);
            cpuNiceDates.add(date);
            cpuNiceValues.add(this.getValue(niceAvg));
            cpuNice.setDates(cpuNiceDates);
            cpuNice.setValues(cpuNiceValues);

            Map<String, Object> irqAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("irq_avg")));
//            System.out.println(date+"-------------"+value);
            cpuIrqDates.add(date);
            cpuIrqValues.add(this.getValue(irqAvg));
            cpuIrq.setDates(cpuIrqDates);
            cpuIrq.setValues(cpuIrqValues);

            Map<String, Object> softirqAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("softirq_avg")));
//            System.out.println(date+"-------------"+value);
            cpuSoftirqDates.add(date);
            cpuSoftirqValues.add(this.getValue(softirqAvg));
            cpuSoftirq.setDates(cpuSoftirqDates);
            cpuSoftirq.setValues(cpuSoftirqValues);

            Map<String, Object> iowaitAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("iowait_avg")));
//            System.out.println(date+"-------------"+value);
            cpuIowaitDates.add(date);
            cpuIowaitValues.add(this.getValue(iowaitAvg));
            cpuIowait.setDates(cpuIowaitDates);
            cpuIowait.setValues(cpuIowaitValues);
        }

        // 装箱
        cpuHistogramUsage.setUser(cpuUser);
        cpuHistogramUsage.setSystem(cpuSystem);
        cpuHistogramUsage.setNice(cpuNice);
        cpuHistogramUsage.setIrq(cpuIrq);
        cpuHistogramUsage.setSoftirq(cpuSoftirq);
        cpuHistogramUsage.setIowait(cpuIowait);

        return cpuHistogramUsage;
    }

    private SystemLoadHistogram getSystemLoadHistogram(Integer interval, SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh1")
                .field("@timestamp")
//                .calendarInterval(DateHistogramInterval.HOUR)
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss");

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("load1_avg").field("system.load.1"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("load5_avg").field("system.load.5"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("load15_avg").field("system.load.15"));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh1");

            // 获取桶聚合结果
            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            return systemLoadForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private SystemLoadHistogram systemLoadForEach(List<? extends Histogram.Bucket> buckets) {
        // 封装
        SystemLoadHistogram systemLoadHistogramUsage = new SystemLoadHistogram();
        Bucket systemLoad1 = new Bucket();
        List<String> systemLoad1Dates = new ArrayList<>();
        List<String> systemLoad1Values = new ArrayList<>();

        Bucket systemLoad5 = new Bucket();
        List<String> systemLoad5Dates = new ArrayList<>();
        List<String> systemLoad5Values = new ArrayList<>();

        Bucket systemLoad15 = new Bucket();
        List<String> systemLoad15Dates = new ArrayList<>();
        List<String> systemLoad15Values = new ArrayList<>();

        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();

            Map<String, Object> load1Avg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("load1_avg")));
//            System.out.println(date+"-------------"+value);
            systemLoad1Dates.add(date);
            systemLoad1Values.add(this.getValue(load1Avg));
            systemLoad1.setDates(systemLoad1Dates);
            systemLoad1.setValues(systemLoad1Values);

            Map<String, Object> load5Avg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("load5_avg")));
//            System.out.println(date+"-------------"+value);
            systemLoad5Dates.add(date);
            systemLoad5Values.add(this.getValue(load5Avg));
            systemLoad5.setDates(systemLoad5Dates);
            systemLoad5.setValues(systemLoad5Values);

            Map<String, Object> load15Avg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("load15_avg")));
//            System.out.println(date+"-------------"+value);
            systemLoad15Dates.add(date);
            systemLoad15Values.add(this.getValue(load15Avg));
            systemLoad15.setDates(systemLoad15Dates);
            systemLoad15.setValues(systemLoad15Values);
        }

        // 装箱
        systemLoadHistogramUsage.setLoad1(systemLoad1);
        systemLoadHistogramUsage.setLoad5(systemLoad5);
        systemLoadHistogramUsage.setLoad15(systemLoad15);

        return systemLoadHistogramUsage;
    }

    private MemoryHistogram getMemoryHistogram(Integer interval, SearchSourceBuilder searchSourceBuilder, SearchRequest searchRequest) {
        // 时间间隔
        DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram("dh2")
                .field("@timestamp")
//                .calendarInterval(DateHistogramInterval.HOUR)
                .fixedInterval(DateHistogramInterval.minutes(interval))
                .timeZone(ZoneId.of("Asia/Shanghai"))
                .format("yyyy-MM-dd hh:mm:ss");

        // 嵌套
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("used_avg").field("system.memory.used.bytes"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("free_avg").field("system.memory.free"));
        dateHistogramAggregationBuilder.subAggregation(AggregationBuilders.avg("cached_avg").field("system.memory.cached"));

        // 聚合查询
        searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
        searchRequest.source(searchSourceBuilder);
        SearchResponse response;

        try {
            //发起请求，获取响应结果
            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

            //获取聚合的结果
            Aggregations aggregations = response.getAggregations();
            Aggregation aggregation = aggregations.get("dh2");

            // 获取桶聚合结果
            List<? extends Histogram.Bucket> buckets = ((Histogram) aggregation).getBuckets();
            return memoryHistogramForEach(buckets);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private MemoryHistogram memoryHistogramForEach(List<? extends Histogram.Bucket> buckets) {
        // 封装
        MemoryHistogram memoryHistogramUsage = new MemoryHistogram();
        Bucket used = new Bucket();
        List<String> usedDates = new ArrayList<>();
        List<String> usedValues = new ArrayList<>();

        Bucket free = new Bucket();
        List<String> freeDates = new ArrayList<>();
        List<String> freeValues = new ArrayList<>();

        Bucket cached = new Bucket();
        List<String> cachedDates = new ArrayList<>();
        List<String> cachedValues = new ArrayList<>();


        // 循环遍历各个桶结果
        for (Histogram.Bucket bucket : buckets) {
            String date = bucket.getKeyAsString();

            Map<String, Object> usedAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("used_avg")));
//            System.out.println(date+"-------------"+value);
            usedDates.add(date);
            usedValues.add(this.converGB(usedAvg));
            used.setDates(usedDates);
            used.setValues(usedValues);

            Map<String, Object> freeAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("free_avg")));
//            System.out.println(date+"-------------"+value);
            freeDates.add(date);
            freeValues.add(this.converGB(freeAvg));
            free.setDates(freeDates);
            free.setValues(freeValues);

            Map<String, Object> cachedAvg =
                    (Map<String, Object>) JSONObject.parse(JSONUtil.toJsonStr(bucket.getAggregations().asMap().get("cached_avg")));
//            System.out.println(date+"-------------"+value);
            cachedDates.add(date);
            cachedValues.add(this.converGB(cachedAvg));
            cached.setDates(cachedDates);
            cached.setValues(cachedValues);
        }

        // 装箱
        memoryHistogramUsage.setUsed(used);
        memoryHistogramUsage.setFree(free);
        memoryHistogramUsage.setCached(cached);

        return memoryHistogramUsage;
    }

    private String getValue(Map<String, Object> map) {
        String value = "";
        if(!Objects.isNull(map.get("value"))) {
            if(!map.get("value").equals(0)) {
                Double doubleValue = ((BigDecimal) map.get("value"))
                        .multiply(new BigDecimal(100))
                        .setScale(3, RoundingMode.HALF_UP)
                        .doubleValue();
                value = doubleValue.toString();// + "%"
            } else {
                value = "0.00";
            }
        } else {
            value = "0.00";
        }
        return value;
    }

/*    -----------------------------------------------------------------------------------------------*/

    private static String conver(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("###.0");

        if (size >= 1024 * 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0 * 1024.0));
            bytes.append(format.format(i)).append("GB");
        }
        else if (size >= 1024 * 1024) {
            double i = (size / (1024.0 * 1024.0));
            bytes.append(format.format(i)).append("MB");
        } else if (size >= 1024) {
            double i = (size / (1024.0));
            bytes.append(format.format(i)).append("KB");
        } else if (size < 1024) {
            if (size <= 0) {
                bytes.append("0B");
            }
            else {
                bytes.append((int) size).append("B");
            }
        }

        return bytes.toString();
    }

    private static String converGB(Map<String, Object> map) {
        StringBuffer bytes = new StringBuffer();
        if(!Objects.isNull(map.get("value"))) {
            if(!map.get("value").equals(0)) {
                Long size = ((BigDecimal) map.get("value")).longValue();
                DecimalFormat format = new DecimalFormat("##0.0");
                double i = (size / (1024.0 * 1024.0 * 1024.0));
                bytes.append(format.format(i));
//                if (size >= 1024 * 1024 * 1024) {
//                    double i = (size / (1024.0 * 1024.0 * 1024.0));
//                    bytes.append(format.format(i));
//                }
//                else if (size >= 1024 * 1024) {
//                    double i = (size / (1024.0 * 1024.0));
//                    bytes.append(format.format(i)).append("MB");
//                } else if (size >= 1024) {
//                    double i = (size / (1024.0));
//                    bytes.append(format.format(i)).append("KB");
//                } else if (size < 1024) {
//                    if (size <= 0) {
//                        bytes.append("0B");
//                    }
//                    else {
//                        bytes.append((int) size.longValue()).append("B");
//                    }
//                }
            } else {
                return "0.00";
            }
        } else {
            return "0.00";
        }

        return bytes.toString();
    }

    private static String converKBS(long size) {
        StringBuffer bytes = new StringBuffer();
        DecimalFormat format = new DecimalFormat("##0.000");

        double i = (size / (1024.0));
        bytes.append(format.format(i));//.append("KB")
//        if (size >= 1024 * 1024 * 1024) {
//            double i = (size / (1024.0 * 1024.0 * 1024.0));
//            bytes.append(format.format(i)).append("GB");
//        }
//        else if (size >= 1024 * 1024) {
//            double i = (size / (1024.0 * 1024.0));
//            bytes.append(format.format(i)).append("MB");
//        } else if (size >= 1024) {
//            double i = (size / (1024.0));
//            bytes.append(format.format(i)).append("KB");
//        } else if (size < 1024) {
//            if (size <= 0) {
//                bytes.append("0B");
//            }
//            else {
//                bytes.append((int) size).append("B");
//            }
//        }

        return bytes.toString();
    }

//    private String getPctUsage(String startTime, String endTime, String must, Integer n,
//                         String one, String two, String three, String four, String five) {
//        //创建一个查询请求，并指定索引名称
//        SearchRequest searchRequest = new SearchRequest("metricbeat-*");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        // 日期区间
//        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
//        boolQuery.must(QueryBuilders.rangeQuery("@timestamp")
//                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
//
//        // 降序
//        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//
//        // 条件查询
//        boolQuery.must(QueryBuilders.matchQuery("metricset.name", must));
//        searchSourceBuilder.query(boolQuery);
//
//        // 查询请求
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse response;
//        try {
//            //发起请求，获取响应结果
//            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            Map<String, Object> map = response.getHits().getAt(1).getSourceAsMap();
//            Double pctSnap = null;
//
//            // 取值
//            switch (n) {
//                case 3:
//                    pctSnap = (Double) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three);
//                    break;
//                case 4:
//                    pctSnap = (Double) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three))
//                            .get(four);
//                    break;
//                case 5:
//                     pctSnap = (Double) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three))
//                            .get(four))
//                            .get(five);
//                    break;
//            }
//
//            // 四舍五入
//            Double pct = new BigDecimal(pctSnap).multiply(new BigDecimal(100))
//                    .setScale(3, RoundingMode.HALF_UP)
//                    .doubleValue();
//
//            return pct.toString();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "";
//    }
//
//    private String getByteUsage(String startTime, String endTime, String must, Integer n,
//                            String one, String two, String three, String four, String five) {
//        //创建一个查询请求，并指定索引名称
//        SearchRequest searchRequest = new SearchRequest("metricbeat-*");
//        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
//
//        // 日期区间
//        BoolQueryBuilder boolQuery = new BoolQueryBuilder();
//        boolQuery.must(QueryBuilders.rangeQuery("@timestamp")
//                .from(startTime + ":59").to(endTime + ":59").timeZone("+08:00"));
//
//        // 降序
//        searchSourceBuilder.sort(new FieldSortBuilder("@timestamp").order(SortOrder.DESC));
//
//        // 条件查询
//        boolQuery.must(QueryBuilders.matchQuery("metricset.name", must));
//        searchSourceBuilder.query(boolQuery);
//
//        // 查询请求
//        searchRequest.source(searchSourceBuilder);
//        SearchResponse response;
//        try {
//            //发起请求，获取响应结果
//            response = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
//            Map<String, Object> map = response.getHits().getAt(1).getSourceAsMap();
//            Long pctSnap = null;
//
//            // 取值
//            switch (n) {
//                case 3:
//                    pctSnap = (Long) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three);
//                    break;
//                case 4:
//                    pctSnap = (Long) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three))
//                            .get(four);
//                    break;
//                case 5:
//                    pctSnap = (Long) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) ((Map<String, Object>) map.get(one))
//                            .get(two))
//                            .get(three))
//                            .get(four))
//                            .get(five);
//                    break;
//            }
//
//            StringBuffer bytes = new StringBuffer();
//            Long size = pctSnap;
//            DecimalFormat format = new DecimalFormat("###.0");
//            if (size >= 1024 * 1024 * 1024) {
//                double i = (size / (1024.0 * 1024.0 * 1024.0));
//                bytes.append(format.format(i)).append("GB");
//            }
//            else if (size >= 1024 * 1024) {
//                double i = (size / (1024.0 * 1024.0));
//                bytes.append(format.format(i)).append("MB");
//            } else if (size >= 1024) {
//                double i = (size / (1024.0));
//                bytes.append(format.format(i)).append("KB");
//            } else if (size < 1024) {
//                if (size <= 0) {
//                    bytes.append("0B");
//                }
//                else {
//                    bytes.append((int) size.longValue()).append("B");
//                }
//            }
//
//            return bytes.toString();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return "";
//    }

}
