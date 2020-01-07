package com.noname;

import com.noname.util.ESUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2020/1/2.
 *
 * 关键字查询，结合了分词器，并高亮显示
 */
public class Search {
    public static void main(String[] args) {
        RestHighLevelClient client = ESUtil.getConnect();
        //指定Index，type
        SearchRequest request = new SearchRequest(ESUtil.INDEX);
        request.types(ESUtil.TYPE);
        /*
        * 条件查询分不同的类型，分别用不同的函数来表示
        * BoolQueryBuilder为接收对象，可以嵌套使用，常用的分为三种组合类型：
        * shoule:或
        * must:与
        * must_not:非
        *
        * 查询类型如下：
        * termQuery:精确条件查询，即：field='关键字'，无视分词器
        * matchQuery:可使用分词器的查询
        *
        * */
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder delQuery = QueryBuilders.termQuery("del_flag", "0");
        TermQueryBuilder activedQUery = QueryBuilders.termQuery("actived", "Y");
        //此处关联关系是must,所以必须同时满足del_flag = 0,actived = Y
        boolQueryBuilder.must(delQuery).must(activedQUery);
        //使用MatchQuery方式，并指定分词器
        BoolQueryBuilder keyTerms = QueryBuilders.boolQuery();
        MatchQueryBuilder title = QueryBuilders.matchQuery("title", "政策").analyzer("ik_max_word");
        MatchQueryBuilder content = QueryBuilders.matchQuery("content", "政策").analyzer("ik_max_word");
        //此段没深入研究，fuzziness是指模糊查询，两个参数是纠正量，还需要研究
        title.fuzziness(Fuzziness.AUTO);
        title.prefixLength(0);
        title.maxExpansions(10);
        content.fuzziness(Fuzziness.AUTO);
        content.prefixLength(0);
        content.maxExpansions(10);
        //此处关联关系是should,所以只要满足任意一个即可
        keyTerms.should(title).should(content);
        //此处是嵌套关系，即相当于sql中的，field = 'a' AND (field = 'b' OR field = 'c')
        boolQueryBuilder.must(keyTerms);


        //设置高亮字段
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field("title");
        highlightTitle.highlighterType("unified");
        HighlightBuilder.Field highlightContent = new HighlightBuilder.Field("content");
        highlightContent.highlighterType("unified");
        highlightBuilder.field(highlightContent).field(highlightTitle);
        sourceBuilder.highlighter(highlightBuilder);


        //分页支持
        sourceBuilder.query(boolQueryBuilder);
        sourceBuilder.from(1);
        sourceBuilder.size(50);
        //如果需要分页就要设置为true
        sourceBuilder.trackTotalHits(true);

        //排序（以指定字段倒序）
        sourceBuilder.sort(new FieldSortBuilder("pub_date").order(SortOrder.DESC));
        //或者以默认的相关度排序
//        sourceBuilder.sort(new ScoreSortBuilder().order(SortOrder.DESC));

        //不要遗漏
        request.source(sourceBuilder);

        try {
            SearchResponse response = client.search(request);
            //response对象中的Hits属性是一个对象，里面还包含了一个hits,此hits是保存了高亮数据的字段
            SearchHits hits = response.getHits();
            SearchHit[] details = hits.getHits();
            //若上面设置了分页为true（sourceBuilder.trackTotalHits(true);），此处可以获取总长度
            long all = hits.getTotalHits();
            System.out.println("一共有" + all + "条数据");

            for (SearchHit hit : details) {
                String source = hit.getSourceAsString();
                System.out.println("元数据：" + source);

                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                //获得拥有高连字段的数据，默认会添加上<em>标签
                HighlightField titleHighlight = highlightFields.get("title");
                if (!Objects.isNull(titleHighlight)) {
                    Text[] titleFragments = titleHighlight.fragments();
                    String titleFrag = titleFragments[0].string();
                    System.out.println("title高亮字段：" + titleFrag);
                }
                HighlightField contentHighlight = highlightFields.get("content");
                if (!Objects.isNull(contentHighlight)) {
                    Text[] contentFragments = contentHighlight.fragments();
                    String contentFrag = contentFragments[0].string();
                    System.out.println("content高亮字段：" + contentFrag);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }
}
