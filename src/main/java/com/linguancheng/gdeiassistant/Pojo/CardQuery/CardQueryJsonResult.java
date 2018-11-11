package com.linguancheng.gdeiassistant.Pojo.CardQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.Card;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CardQueryJsonResult {

    private boolean success;

    private String message;

    private CardInfo cardInfo;

    private List<Card> cardList;

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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
