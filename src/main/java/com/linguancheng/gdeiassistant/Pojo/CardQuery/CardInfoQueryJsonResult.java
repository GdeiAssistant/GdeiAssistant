package com.linguancheng.gdeiassistant.Pojo.CardQuery;

import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CardInfoQueryJsonResult {

    private boolean success;

    private String message;

    private CardInfo cardInfo;

    public CardInfo getCardInfo() {
        return cardInfo;
    }

    public void setCardInfo(CardInfo cardInfo) {
        this.cardInfo = cardInfo;
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
