package com.example.elasticsearch.demo;

import com.alibaba.fastjson.JSONObject;
import com.example.elasticsearch.demo.model.BaseInfo;
import com.example.elasticsearch.demo.util.DataUtil;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

    @Resource
    private RestHighLevelClient client;

    @Test
    public void createIndexRequest() throws IOException {
        final String CREATE_INDEX = "{\n" +
                "    \"properties\": {\n" +
                "      \"projectName\":{\n" +
                "        \"type\":\"text\",\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"search_analyzer\": \"ik_smart\"\n" +
                "      },\n" +
                "      \"shortName\":{\n" +
                "        \"type\":\"text\",\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"search_analyzer\": \"ik_smart\"\n" +
                "      },\n" +
                "      \"businessAddress\":{\n" +
                "        \"type\":\"text\",\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"search_analyzer\": \"ik_smart\"\n" +
                "      },\n" +
                "      \"businessModel\":{\n" +
                "        \"type\":\"text\",\n" +
                "        \"index\": true,\n" +
                "        \"analyzer\": \"ik_max_word\",\n" +
                "        \"search_analyzer\": \"ik_smart\"\n" +
                "      }\n" +
                "    }\n" +
                "  }";
        CreateIndexRequest request = new CreateIndexRequest("index_baseinfo_bigdata");
        request.settings(Settings.builder().put("index.number_of_shards", 3).put("index.number_of_replicas", 2));
        request.mapping(CREATE_INDEX, XContentType.JSON);
        CreateIndexResponse res = client.indices().create(request, RequestOptions.DEFAULT);
    }


    @Test
    public void insertOrUpdate() throws Exception {
        IndexRequest request = new IndexRequest("index_baseinfo_bigdata");
        List<BaseInfo> list = DataUtil.getBaseInfoList();
        for (int i = 0; i < 1000; i++) {
            for (BaseInfo t : list) {
                request.source(JSONObject.toJSONString(t), XContentType.JSON);
                IndexResponse a = client.index(request, RequestOptions.DEFAULT);
            }
        }
    }

    @Test
    public void bulkInsert() throws Exception {
        BulkRequest request = new BulkRequest();
        List<BaseInfo> list = DataUtil.getBaseInfoList();
        for (int i = 0; i < 1000; i++) {
            for (BaseInfo t : list) {
                request.add(new IndexRequest("index_baseinfo_bigdata").source(JSONObject.toJSONString(t), XContentType.JSON));
            }
        }
        BulkResponse a = client.bulk(request, RequestOptions.DEFAULT);
    }

    @Test
    public void searchIndex() {
        SearchRequest request = new SearchRequest("index_baseinfo_ik");
        SearchSourceBuilder s1 = SearchSourceBuilder.searchSource();
        request.source(s1);
        try {
            SearchResponse response = client.search(request, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            System.out.println(hits);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void searchAPI() throws Exception {
        // 构造参数可选，设置查询的index名称
        SearchRequest searchRequest = new SearchRequest();
        // 设置 routing 参数
        //searchRequest.routing("routing");
        // 配置搜索时偏爱使用本地分片，默认是使用随机分片
        searchRequest.preference("_local");


        /*
         QueryBuilders开始
         */
        //
        QueryBuilder allQueryBuilder = QueryBuilders.matchAllQuery();

        // 测试查询2
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("projectName", "公司");

        // 测试查询3
        QueryBuilder stringQueryBuilder = QueryBuilders.queryStringQuery("北京");

        // 测试查询4
        QueryBuilder termQueryBuilder = QueryBuilders.termQuery("projectName", "公司");
        //测试查询5
        QueryBuilder multiMatchQuery = QueryBuilders.multiMatchQuery("北京", "projectName", "businessModel", "businessAddress", "shortName");

        //matchQuery.prefixLength(3); // 前缀查询的长度
        //matchQuery.maxExpansions(10); // max expansion 选项，用来控制模糊查询
        /*// 匹配查询写法2：Builders写法
        QueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", "quick brown fox")
                .fuzziness(Fuzziness.AUTO)
                .prefixLength(3)
                .maxExpansions(10);*/

        /*
         QueryBuilders结束
         */


        /*
        高量开始
         */
        /*HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        highlightBuilder.field("projectName").field("shortName");*/
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style=\"color:red\">");
        highlightBuilder.postTags("</span>");
        HighlightBuilder.Field field1 = new HighlightBuilder.Field("projectName");
        field1.highlighterType("unified");
        highlightBuilder.field(field1);
        HighlightBuilder.Field field2 = new HighlightBuilder.Field("shortName");
        highlightBuilder.field(field2);
        highlightBuilder.field("businessModel");
        highlightBuilder.field("businessAddress");
        /*
        高量结束
         */



        /*
         配置Source开始
         */
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        // 1.查询条件 QueryBuilder
        searchSourceBuilder.query(stringQueryBuilder);
        // 2. 排序条件
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); // 根据分数 _score 降序排列 (默认行为)
        // searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.ASC));  // 根据 id 降序排列
        // 3. 高亮highlightBuilder
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);

        /*
        配置source结束
         */
        searchRequest.source(searchSourceBuilder);

        // 开始查询
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();

        List<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            //将文档中的每一个对象转换json串值
            Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
            //获取对应的高亮域
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            highlightFields.forEach((k, v) -> {
                if (v != null) {
                    sourceAsMap.put(k, StringUtils.strip(Arrays.toString(v.fragments()), "[]"));
                }
            });
            list.add(sourceAsMap);
        }
        System.out.println(list);
        client.close();
    }

}
