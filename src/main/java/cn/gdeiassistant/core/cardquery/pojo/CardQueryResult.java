package cn.gdeiassistant.core.cardquery.pojo;

import cn.gdeiassistant.common.pojo.Entity.Card;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CardQueryResult {

    @JsonProperty("cardQuery")
    private CardQuery cardQuery;

    @JsonProperty("cardInfo")
    private CardInfo cardInfo;

    @JsonProperty("cardList")
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
