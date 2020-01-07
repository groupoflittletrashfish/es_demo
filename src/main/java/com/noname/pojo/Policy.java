package com.noname.pojo;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Administrator on 2019/12/20.
 */
@Data
public class Policy implements Serializable {

    private String num;
    private String refCityNum;
    private String cityCode;
    private Integer year;
    private Date pubDate;
    private String tag;
    private String tagType;
    private String title;
    private String content;
    private String source;
    private String url;
    private String actived;
    private Integer version;
    private String delFlag;
    private String createdBy;
    private Date createdTime;
    private String updatedBy;
    private Date updatedTime;
    private String cityName;
    private String tagName;
}
