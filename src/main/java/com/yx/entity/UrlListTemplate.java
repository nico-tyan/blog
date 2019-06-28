package com.yx.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.enums.IdType;

/**
 * url模板设定
 * @Title: 
 * @Package com.yx.entity  
 * @Description:  
 * @date 2019年6月10日  
 * @version
 */
public class UrlListTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String url;
    private String listRegurl;
    private String detailRegurl;
    private String downType;
    private String domain;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getListRegurl() {
        return listRegurl;
    }

    public void setListRegurl(String listRegurl) {
        this.listRegurl = listRegurl;
    }

    public String getDetailRegurl() {
        return detailRegurl;
    }

    public void setDetailRegurl(String detailRegurl) {
        this.detailRegurl = detailRegurl;
    }

    public String getDownType() {
        return downType;
    }

    public void setDownType(String downType) {
        this.downType = downType;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String toString() {
        return "UrlListTemplate{" +
        "id=" + id +
        ", url=" + url +
        ", listRegurl=" + listRegurl +
        ", detailRegurl=" + detailRegurl +
        ", downType=" + downType +
        ", domain=" + domain +
        "}";
    }
}
