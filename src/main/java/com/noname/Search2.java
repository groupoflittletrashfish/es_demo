package com.noname;

import com.noname.util.ESUtil;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2020/1/6.
 *
 */
public class Search2 {

    public static void main(String[] args) {
        RestHighLevelClient client = ESUtil.getConnect();
        SearchRequest request = new SearchRequest(ESUtil.INDEX);
        request.types(ESUtil.TYPE);

        //添加过滤条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        TermQueryBuilder delQuery = QueryBuilders.termQuery("del_flag", "0");
        TermQueryBuilder activeQuery = QueryBuilders.termQuery("actived", "Y");
        boolQueryBuilder.must(delQuery).must(activeQuery);

        BoolQueryBuilder keysTerms = QueryBuilders.boolQuery();
        MatchQueryBuilder title = QueryBuilders.matchQuery("title", "政策").analyzer("ik_max_word");
        MatchQueryBuilder content = QueryBuilders.matchQuery("content", "政策").analyzer("ik_max_word");
        title.fuzziness(Fuzziness.AUTO);
        title.prefixLength(0);
        title.maxExpansions(10);
        content.fuzziness(Fuzziness.AUTO);
        content.prefixLength(0);
        content.maxExpansions(10);
        keysTerms.should(title).should(content);
        boolQueryBuilder.must(keysTerms);
        sourceBuilder.query(boolQueryBuilder);

        //分组聚合
        /*下面两行中的by_city和by_cityName是一个别名,可自定义，.field中的字段是用来分组的字段，相当于group by field,
          一般情况下，如果只以一个字段分组，就不需要.subAggregation函数，此函数表示父子关系，即先以A字段分组，
          然后再以B字段分组，相当于是group by fieldA,fieldB
        */

        TermsAggregationBuilder byCity = AggregationBuilders.terms("by_city").field("ref_city_num");
        TermsAggregationBuilder byCityName = AggregationBuilders.terms("by_cityName").field("city_name");
        byCity.subAggregation(byCityName);
        TermsAggregationBuilder byTag = AggregationBuilders.terms("by_tag").field("tag_type");
        TermsAggregationBuilder byTagName = AggregationBuilders.terms("by_tagName").field("tag_name");
        byTag.subAggregation(byTagName);
        /*
            下面这段也是分组，不过不同的是先做了数据处理再聚合。by_pubDate也是指别名，可自定义，
            dateHistogramInterval(DateHistogramInterval.YEAR) 这段是指从时间内抽出年份聚合，
            不同的类型需要使用不同的函数，自行百度
         */
        DateHistogramAggregationBuilder byPubDate = AggregationBuilders.dateHistogram("by_pubDate").field("pub_date").dateHistogramInterval(DateHistogramInterval.YEAR)
                .format("yyyy")
                .minDocCount(0L);

        //排序，如果是false,则是默认根据聚合数量的从大到小排序
        byCity.order(BucketOrder.count(false));
        byCity.size(Integer.MAX_VALUE);

        byTag.order(BucketOrder.count(false));
        byTag.size(Integer.MAX_VALUE);

        byPubDate.order(BucketOrder.count(false));

        sourceBuilder.aggregation(byCity);
        sourceBuilder.aggregation(byTag);
        sourceBuilder.aggregation(byPubDate);
        //不要遗漏
        request.source(sourceBuilder);

        try {
            /*
            *ES支持多重聚合，即以不同的维度聚合，并一起处理，上面的代码分别以城市，政策标签和年份
            * 做了聚合，但其结果是不相互影响的，相当于是一个桶的概念，只是将不同的聚合方式放在一个
            * 桶中，如下便是获取桶中的不同的结果集
            *
            */
            SearchResponse response = client.search(request);
            //获取聚合的结果并转换为Map
            Map<String, Aggregation> aggMap = response.getAggregations().asMap();
            //此处的by_city就是指上面的别名，获取城市聚合的结果
            ParsedStringTerms cityAgg = (ParsedStringTerms) aggMap.get("by_city");
            for (Terms.Bucket buck : cityAgg.getBuckets()) {
                String cityNum = buck.getKeyAsString();
                long cityCount = buck.getDocCount();
                System.out.println("cityNum:" + cityNum + ",cityCount:" + cityCount);
                //此处的by_cityName也是上面的别名，by_cityName是by_city的子聚合，所以先要获得by_city然后再获取
                ParsedStringTerms nameAgg = (ParsedStringTerms) buck.getAggregations().asMap().get("by_cityName");
                for (Terms.Bucket nameBuck : nameAgg.getBuckets()) {
                    String cityName = nameBuck.getKeyAsString();
                    System.out.println("cityNum:" + cityName);
                }
            }

            ParsedStringTerms tagAgg = (ParsedStringTerms) aggMap.get("by_tag");
            for (Terms.Bucket buck : tagAgg.getBuckets()) {
                String tagNum = buck.getKeyAsString();
                long tagCount = buck.getDocCount();
                System.out.println("tagNum:" + tagNum + ",tagCount:" + tagCount);

                ParsedStringTerms nameAgg = (ParsedStringTerms) buck.getAggregations().asMap().get("by_tagName");
                for (Terms.Bucket nameBuck : nameAgg.getBuckets()) {
                    String tagName = nameBuck.getKeyAsString();
                    System.out.println("tagName:" + tagName);
                }
            }

            ParsedDateHistogram pubDate = (ParsedDateHistogram) aggMap.get("by_pubDate");
            for (Histogram.Bucket buck : pubDate.getBuckets()) {
                String YearNum = buck.getKeyAsString();
                long YearCount = buck.getDocCount();
                System.out.println("yearNum:" + YearNum + ",yearCount:" + YearCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }
}
