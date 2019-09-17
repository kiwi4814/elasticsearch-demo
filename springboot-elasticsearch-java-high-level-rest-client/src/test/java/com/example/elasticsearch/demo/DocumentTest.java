package com.example.elasticsearch.demo;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

@RunWith(SpringRunner.class)
@SpringBootTest
public class DocumentTest {

    @Resource
    private RestHighLevelClient client;


    /**
     * 查询全部
     */
    @Test
    public void allQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.matchAllQuery();
        System.out.println(searchByQueryBuilder(client, queryBuilder));
    }


    @Test
    public void matchQuery() throws IOException {
        // 不行
        QueryBuilder queryBuilder = QueryBuilders.matchQuery("file", "北汽").queryName("filename");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    /**
     * 多个匹配查询
     */
    @Test
    public void multiMatchQuery() throws Exception {
        // 查询projectName、shortName、businessAddress、businessMode这四个字段中含有北京的条目，支持通配符，字段名区分大小写

        QueryBuilder queryBuilder4 = QueryBuilders.multiMatchQuery("金食", "projectName", "shortName", "businessModel", "businessAddress")
                .operator(Operator.OR)
                //.field("updateTime")
                //.fuzziness(Fuzziness.AUTO)
                //.analyzer("ik_smart")
                //.type(MultiMatchQueryBuilder.Type.BEST_FIELDS)
                ;
        // 查询以Name结尾或以business开头的字段中含有北京的
//        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("北京", "*Name", "business*", "shortName");
        // 不提供字段的时候默认为全部字段，上限为1024
        QueryBuilder queryBuilder = QueryBuilders.multiMatchQuery("北汽", "content", "file.path", "file.filename");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void termQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.termQuery("file.filename", "gog");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void termsQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.termsQuery("file.filename", "阿里", "北汽", "卓沃", "测试");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }

    @Test
    public void nestQuery() throws Exception {
        QueryBuilder nestQuery = QueryBuilders.nestedQuery("file",
                QueryBuilders.boolQuery()
                        .must(QueryBuilders.matchQuery("filename", "")),
                ScoreMode.Total);

        System.out.println(searchByQueryBuilder(client, nestQuery));
        client.close();
    }

    @Test
    public void boolQuery() throws Exception {
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        // 名称中包含北京
        QueryBuilder nameQueryBuilder = QueryBuilders.termQuery("projectName", "宁德");
        boolQueryBuilder.must(nameQueryBuilder);
        // 时间大于43671
        RangeQueryBuilder timeQueryBuilder = QueryBuilders.rangeQuery("updateTime").gt(43671);
        boolQueryBuilder.must(timeQueryBuilder);
        // id范围
        int[] ids = {93, 94, 95, 96, 97, 98, 99, 124, 125, 126, 127, 128};
        TermsQueryBuilder idQueryBuilder = QueryBuilders.termsQuery("id", ids);
        boolQueryBuilder.must(idQueryBuilder);

        System.out.println(searchByQueryBuilder(client, boolQueryBuilder));
        client.close();
    }

    @Test
    public void stringQuery() throws Exception {
        QueryBuilder queryBuilder = QueryBuilders.queryStringQuery("content");
        System.out.println(searchByQueryBuilder(client, queryBuilder));
        client.close();
    }


    private static List searchByQueryBuilder(RestHighLevelClient client, QueryBuilder queryBuilder) throws IOException {
        SearchRequest searchRequest = new SearchRequest("job_name_test14");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder);
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
        System.out.println(list);
        client.close();
        return list;
    }
}

