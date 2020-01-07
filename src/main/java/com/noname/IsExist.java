package com.noname;

import com.noname.util.ESUtil;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import java.io.IOException;

/**
 * Created by Administrator on 2020/1/2.
 * <p>
 * 查看Id是否存在
 */
public class IsExist {

    public static void main(String[] args) {
        RestHighLevelClient client = ESUtil.getConnect();
        GetRequest request = new GetRequest(ESUtil.INDEX, ESUtil.TYPE, "8888888");
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        try {
            System.out.println(client.exists(request));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }
}
