package cn.gdeiassistant.core.collectionquery.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Collection;

import java.io.Serializable;
import java.util.List;

/**
 * 馆藏检索结果 VO。
 */
public class CollectionQueryVO implements Serializable {

    private Integer sumPage;
    private List<Collection> collectionList;

    public Integer getSumPage() { return sumPage; }
    public void setSumPage(Integer sumPage) { this.sumPage = sumPage; }
    public List<Collection> getCollectionList() { return collectionList; }
    public void setCollectionList(List<Collection> collectionList) { this.collectionList = collectionList; }
}
