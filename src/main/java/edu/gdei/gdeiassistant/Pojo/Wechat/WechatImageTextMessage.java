package edu.gdei.gdeiassistant.Pojo.Wechat;

import edu.gdei.gdeiassistant.Enum.Wechat.MessageTypeEnum;

import java.util.List;

public class WechatImageTextMessage extends WechatBaseMessage {

    //图文消息个数，限制为8条以内
    private Integer ArticleCount;

    //多条图文消息信息，默认第一个item为大图,注意，如果图文数超过8，则将会无响应
    private List<WechatArticle> Articles;

    public Integer getArticleCount() {
        return ArticleCount;
    }

    public void setArticleCount(Integer articleCount) {
        ArticleCount = articleCount;
    }

    public List<WechatArticle> getArticles() {
        return Articles;
    }

    public void setArticles(List<WechatArticle> articles) {
        Articles = articles;
    }

    public WechatImageTextMessage() {
        super.setMsgType(MessageTypeEnum.IMAGE_TEXT.getType());
    }

    public WechatImageTextMessage(WechatBaseMessage wechatBaseMessage) {
        super.setFromUserName(wechatBaseMessage.getFromUserName());
        super.setToUserName(wechatBaseMessage.getToUserName());
        super.setCreateTime(wechatBaseMessage.getCreateTime());
        super.setMsgType(MessageTypeEnum.IMAGE_TEXT.getType());
    }

    public WechatImageTextMessage(WechatBaseMessage wechatBaseMessage, Integer ArticleCount, List<WechatArticle> Articles) {
        super.setFromUserName(wechatBaseMessage.getFromUserName());
        super.setToUserName(wechatBaseMessage.getToUserName());
        super.setCreateTime(wechatBaseMessage.getCreateTime());
        super.setMsgType(MessageTypeEnum.IMAGE_TEXT.getType());
        this.setArticleCount(ArticleCount);
        this.setArticles(Articles);
    }
}
