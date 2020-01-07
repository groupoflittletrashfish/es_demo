package com.noname;

import com.noname.util.ESUtil;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;

/**
 * Created by Administrator on 2019/12/11.
 * <p>
 * 实例为最简单的主函数运行方式，springboot提供了es的融合，此处只展示基础功能部分，
 * <p>
 * 创建索引
 */
public class CreateIndex {
    public static void main(String[] args) {
        RestHighLevelClient client = ESUtil.getConnect();
        CreateIndexRequest request = new CreateIndexRequest(ESUtil.INDEX);
        //分片设置
        request.settings(Settings.builder()
                .put("index.number_of_shards", 3)
                .put("index.number_of_replicas", 2));
        try {
            //XContentBuilder 工具是JSON数据的格式,也可以Map方式，比较推荐这两种
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            {
                builder.startObject("properties");
                {
                    builder.startObject("num");             //字段名
                    {
                        builder.field("type", "keyword");   //字段类型，详见【概念.txt】
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("ref_city_num");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("city_code");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("pub_date");
                    {
                        builder.field("type", "date");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("tag");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("tag_type");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("title");
                    {
                        builder.field("type", "text");
                        builder.field("analyzer", "ik_max_word");       //指定分词器，ik是需要下载的，百度云中已有对应版本
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("content");
                    {
                        builder.field("type", "text");
                        builder.field("analyzer", "ik_max_word");       //指定分词器，ik是需要下载的，百度云中已有对应版本
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("source");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("url");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("actived");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("version");
                    {
                        builder.field("type", "integer");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("del_flag");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("created_by");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("created_time");
                    {
                        builder.field("type", "date");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("updated_by");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("updated_time");
                    {
                        builder.field("type", "date");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("year");
                    {
                        builder.field("type", "integer");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("tag_name");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                {
                    builder.startObject("city_name");
                    {
                        builder.field("type", "keyword");
                    }
                    builder.endObject();
                }
                builder.endObject();
            }
            builder.endObject();
            //mapping方法是创建字段的映射关系，即每个字段的类型等
            request.mapping(ESUtil.TYPE, builder);
            //有同步操作和异步操作之分，此项目统一使用同步方式
            CreateIndexResponse createIndexResponse = client.indices().create(request);
            System.out.println("成功与否:" + createIndexResponse.isAcknowledged());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }

}
