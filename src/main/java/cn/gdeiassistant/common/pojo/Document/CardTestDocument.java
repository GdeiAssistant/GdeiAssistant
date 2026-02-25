package cn.gdeiassistant.common.pojo.Document;

import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * MongoDB 集合 "card"：测试账号饭卡信息与消费流水模拟数据。
 * 文档字段：username, cardInfo, cardRecordList
 */
@Document(collection = "card")
public class CardTestDocument {

    @Id
    private String id;

    private String username;

    /** 饭卡基本信息，对应 GET /api/card/info */
    private CardInfo cardInfo;

    /** 消费流水列表，对应 POST /api/card/query */
    private List<Card> cardRecordList;

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

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public List<Card> getCardRecordList() {
        return cardRecordList;
    }

    public void setCardRecordList(List<Card> cardRecordList) {
        this.cardRecordList = cardRecordList;
    }
}

