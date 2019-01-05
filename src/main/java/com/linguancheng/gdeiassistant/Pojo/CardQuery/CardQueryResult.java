package com.linguancheng.gdeiassistant.Pojo.CardQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.Card;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CardQueryResult {

    private CardQuery cardQuery;

    private CardInfo cardInfo;

    private List<Card> cardList;

    public CardQuery getCardQuery() {
        return cardQuery;
    }

    public void setCardQuery(CardQuery cardQuery) {
        this.cardQuery = cardQuery;
    }

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
    }

    public List<Card> getCardList() {
        return cardList;
    }

    public void setCardList(List<Card> cardList) {
        this.cardList = cardList;
    }
}
