package com.noname;

import com.alibaba.fastjson.JSONArray;
import com.noname.dao.PolicyDao;
import com.noname.pojo.Policy;
import com.noname.util.ESUtil;
import com.noname.util.SpringUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by Administrator on 2019/12/11.
 *
 */
public class Insert {
    public static void main(String[] args) {
        SpringApplication.run(EsDemoApplication.class, args);


        RestHighLevelClient client = ESUtil.getConnect();
        //数据读取
        ApplicationContext context = SpringUtil.getApplicationContext();
        PolicyDao dao = context.getBean(PolicyDao.class);
        List<Policy> all = dao.getAll();
        System.out.println(all);

        //数据插入(批量数据插入)
        BulkRequest request = new BulkRequest();
        for (int x = 0; x < all.size(); x++) {
            Policy policy = all.get(x);
            JSONArray tags = null;
            if (!Objects.isNull(policy.getTag())) {
                tags = new JSONArray(Arrays.asList(policy.getTag()));
            }
            request.add(new IndexRequest(ESUtil.INDEX, ESUtil.TYPE, String.valueOf(x + 1)).source(
                    XContentType.JSON, "num", policy.getNum(), "ref_city_num", policy.getRefCityNum(),
                    "city_code", policy.getCityCode(), "pub_date", policy.getPubDate(), "tag", tags,
                    "tag_type", policy.getTagType(), "title", policy.getTitle(), "content", policy.getContent(),
                    "source", policy.getSource(), "url", policy.getUrl(), "actived", policy.getActived(),
                    "version", policy.getVersion(), "del_flag", policy.getDelFlag(), "created_by", policy.getCreatedBy(),
                    "created_time", policy.getCreatedTime(), "updated_by", policy.getUpdatedBy(), "updated_time", policy.getUpdatedTime(),
                    "year", policy.getYear(), "city_name", policy.getCityName(), "tag_name", policy.getTagName()
            ));
        }
        try {
            client.bulk(request);
            System.out.println("批量插入成功！");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            ESUtil.close(client);
        }
    }
}
