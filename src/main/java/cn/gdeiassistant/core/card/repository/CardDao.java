package cn.gdeiassistant.core.card.repository;

import cn.gdeiassistant.common.pojo.Document.CardTestDocument;

/**
 * MongoDB 集合 "card"：测试账号饭卡信息与消费流水。
 */
public interface CardDao {

    /**
     * 按用户名查询整份饭卡文档 cardTestDocument，无则返回 null。
     */
    CardTestDocument queryCardDocumentByUsername(String username);
}

