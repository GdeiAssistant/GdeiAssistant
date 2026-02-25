package cn.gdeiassistant.common.pojo.Document;

import cn.gdeiassistant.common.pojo.Entity.Collection;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB 集合 "collection"：测试账号馆藏检索与详情模拟数据。
 * 文档字段：username, collectionList, collectionDetailList
 */
@Document(collection = "collection")
public class CollectionTestDocument {

    @Id
    private String id;

    private String username;

    /** 馆藏检索列表（对应 GET /api/book/search） */
    private List<Collection> collectionList;

    /** 馆藏详情列表，按 detailURL 匹配（对应 GET /api/book/detail） */
    private List<CollectionDetailEntry> collectionDetailList;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<Collection> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<Collection> collectionList) {
        this.collectionList = collectionList;
    }

    public List<CollectionDetailEntry> getCollectionDetailList() {
        return collectionDetailList;
    }

    public void setCollectionDetailList(List<CollectionDetailEntry> collectionDetailList) {
        this.collectionDetailList = collectionDetailList;
    }
}
