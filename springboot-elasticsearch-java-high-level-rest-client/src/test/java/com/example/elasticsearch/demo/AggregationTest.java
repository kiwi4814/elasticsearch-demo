package com.example.elasticsearch.demo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.MultiBucketsAggregation;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class AggregationTest {

    @Resource
    private RestHighLevelClient client;


    /**
     * 查询全部
     */
    @Test
    public void allQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        // 获取所有的index及条数
        AggregationBuilder aggregationBuilderByIndex = AggregationBuilders.terms("by_index").field("_index");
        // 获取所有的type及条数
        AggregationBuilder aggregationBuilderByType = AggregationBuilders.terms("by_type").field("_type");
        // 获取最大时间
        AggregationBuilder aggregationBuilderMaxTime = AggregationBuilders.stats("test_status").field("file.filesize");
        System.out.println(searchByQueryBuilder(client, queryBuilder, aggregationBuilderByType, aggregationBuilderMaxTime, aggregationBuilderByIndex));
    }


    private static List searchByQueryBuilder(RestHighLevelClient client, QueryBuilder queryBuilder, AggregationBuilder... aggregationBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
        if (aggregationBuilder != null)
            for (AggregationBuilder aggregationb : aggregationBuilder) {
                searchSourceBuilder.aggregation(aggregationb);
            }
        searchSourceBuilder.from(0).size(10000);
        searchRequest.source(searchSourceBuilder);

        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false);
        HighlightBuilder.Field field1 = new HighlightBuilder.Field("content").preTags("<span style=\"color:red\">").postTags("</span>");
        highlightBuilder.field(field1);
        HighlightBuilder.Field field2 = new HighlightBuilder.Field("file.filename").preTags("<span style=\"color:blue\">").postTags("</span>");
        highlightBuilder.field(field2);

        searchSourceBuilder.highlighter(highlightBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();
        SearchHit[] hits = searchHits.getHits();
        List<JSONObject> list = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            String s = searchHit.getSourceAsString();
            JSONObject jsonObject = JSONObject.parseObject(searchHit.getSourceAsString());
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            // 获取高亮值并且更新到jsonObject的原始值当中
            highlightFields.forEach((k, v) ->
                    Optional.ofNullable(v).map(HighlightField::getFragments)
                            .ifPresent(fragments ->
                                    JSONPath.set(jsonObject,
                                            "$." + k,
                                            StringUtils.strip(Arrays.toString(fragments), "[]")))
            );
            list.add(jsonObject);
        }
        Map<String, Aggregation> aggregationMap = response.getAggregations().getAsMap();
        ParsedStringTerms parsedStringTerms = (ParsedStringTerms) aggregationMap.get("by_type");
        List<? extends Terms.Bucket> bucketMap = parsedStringTerms.getBuckets();
        Map<String, Object> resultMap = bucketMap.stream().collect(Collectors.toMap(
                MultiBucketsAggregation.Bucket::getKeyAsString, MultiBucketsAggregation.Bucket::getDocCount));
        System.out.println(resultMap);
        client.close();
        return list;
    }
}

