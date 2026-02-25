package cn.gdeiassistant.common.pojo.Document;

import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;

import java.io.Serializable;

/**
 * MongoDB collection 集合中馆藏详情条目：按 detailURL 匹配返回 CollectionDetail。
 */
public class CollectionDetailEntry implements Serializable {

    private String detailURL;
    private CollectionDetail detail;

    public String getDetailURL() {
        return detailURL;
    }

    public void setDetailURL(String detailURL) {
        this.detailURL = detailURL;
    }

    public CollectionDetail getDetail() {
        return detail;
    }

    public void setDetail(CollectionDetail detail) {
        this.detail = detail;
    }
}
