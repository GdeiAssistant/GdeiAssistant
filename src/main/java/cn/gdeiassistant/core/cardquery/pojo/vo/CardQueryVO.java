package cn.gdeiassistant.core.cardquery.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.core.cardquery.pojo.dto.CardQueryDTO;

import java.io.Serializable;
import java.util.List;

/**
 * 饭卡查询结果 VO。
 */
public class CardQueryVO implements Serializable {

    private CardQueryDTO cardQuery;
    private CardInfo cardInfo;
    private List<Card> cardList;

    public CardQueryDTO getCardQuery() { return cardQuery; }
    public void setCardQuery(CardQueryDTO cardQuery) { this.cardQuery = cardQuery; }
    public CardInfo getCardInfo() { return cardInfo; }
    public void setCardInfo(CardInfo cardInfo) { this.cardInfo = cardInfo; }
    public List<Card> getCardList() { return cardList; }
    public void setCardList(List<Card> cardList) { this.cardList = cardList; }
}
