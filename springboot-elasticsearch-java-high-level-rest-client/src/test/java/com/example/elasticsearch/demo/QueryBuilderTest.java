package com.example.elasticsearch.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.elasticsearch.demo.model.BaseInfo;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class QueryBuilderTest {

    @Resource
    private RestHighLevelClient client;


    @Test
    public void allQueryBuilder() throws Exception {
        SearchRequest searchRequest = new SearchRequest("index_baseinfo_ik");
        searchRequest.preference("_local");
        QueryBuilder allQueryBuilder = QueryBuilders.matchAllQuery().queryName("德");



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
        searchSourceBuilder.query(allQueryBuilder);
        // 2. 排序条件
        searchSourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC)); // 根据分数 _score 降序排列 (默认行为)
        // searchSourceBuilder.sort(new FieldSortBuilder("id").order(SortOrder.ASC));  // 根据 id 降序排列
        // 3. 高亮highlightBuilder
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(100);

        /*
        配置source结束
         */
        searchRequest.source(searchSourceBuilder);

        // 开始查询
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        SearchHits searchHits = response.getHits();
        long total = searchHits.getTotalHits().value;
        SearchHit[] hits = searchHits.getHits();

        List<BaseInfo> list = new ArrayList<>();
        for (SearchHit searchHit : hits) {
            //将文档中的每一个对象转换json串值
            String sourceAsString = searchHit.getSourceAsString();
            //将json串值转换成对应的实体对象
            BaseInfo bs = JSON.parseObject(sourceAsString, new TypeReference<BaseInfo>() {
            });
            //获取对应的高亮域
            Map<String, HighlightField> highlightFields = searchHit.getHighlightFields();
            //高亮字段
            HighlightField highlight1 = highlightFields.get("projectName");
            if (highlight1 != null) {
                Text[] titleTexts = highlight1.fragments();
                StringBuilder text1 = new StringBuilder();
                for (Text text : titleTexts) {
                    text1.append(text);
                }
                bs.setProjectName(text1.toString());
            }
            HighlightField highlight2 = highlightFields.get("shortName");
            if (highlight2 != null) {
                Text[] titleTexts = highlight2.fragments();
                StringBuilder text2 = new StringBuilder();
                for (Text text : titleTexts) {
                    text2.append(text);
                }
                bs.setShortName(text2.toString());
            }
            HighlightField highlight3 = highlightFields.get("businessAddress");
            if (highlight3 != null) {
                Text[] titleTexts = highlight3.fragments();
                StringBuilder text3 = new StringBuilder();
                for (Text text : titleTexts) {
                    text3.append(text);
                }
                bs.setBusinessAddress(text3.toString());
            }
            HighlightField highlight4 = highlightFields.get("businessModel");
            if (highlight4 != null) {
                Text[] titleTexts = highlight4.fragments();
                StringBuilder text4 = new StringBuilder();
                for (Text text : titleTexts) {
                    text4.append(text);
                }
                bs.setBusinessModel(text4.toString());
            }
            list.add(bs);
        }
        System.out.println(list);
    }

}
