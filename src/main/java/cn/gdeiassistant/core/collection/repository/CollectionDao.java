package cn.gdeiassistant.core.collection.repository;

import cn.gdeiassistant.common.pojo.Entity.Collection;
import cn.gdeiassistant.common.pojo.Entity.CollectionDetail;

import java.util.List;

/**
 * MongoDB 集合 "collection"：测试账号馆藏检索与详情。
 */
public interface CollectionDao {

    /**
     * 按用户名查询馆藏列表 collectionList，无则返回空列表。
     */
    List<Collection> queryCollectionListByUsername(String username);

    /**
     * 按用户名和 detailURL 从 collectionDetailList 中匹配并返回 CollectionDetail，无则返回 null。
     */
    CollectionDetail queryCollectionDetailByDetailURL(String username, String detailURL);
}
