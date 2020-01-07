package com.noname;

import com.google.common.collect.Maps;
import com.noname.util.ESUtil;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2019/12/28.
 * <p>
 * ES不区分增改操作，若有对应id则是修改，若无对应id则是添加
 */
public class UpdateAndInsertDocument {

    public static void main(String[] args) {
        RestHighLevelClient client = ESUtil.getConnect();

        try {
            IndexRequest request = new IndexRequest(ESUtil.INDEX, ESUtil.TYPE, "99999");
            Map<String, Object> jsonMap = Maps.newHashMap();
            jsonMap.put("num", 99998);
            jsonMap.put("ref_city_num", "110000");
            jsonMap.put("city_code", "北京");
            jsonMap.put("title", "测试添加/更新单条记录");
            request.source(jsonMap);
            IndexResponse response = client.index(request);
            if (response.getResult() == DocWriteResponse.Result.CREATED) {
                System.out.println("创建数据成功...");
            }
            if (response.getResult() == DocWriteResponse.Result.UPDATED) {
                System.out.println("更新数据成功...");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }
}
