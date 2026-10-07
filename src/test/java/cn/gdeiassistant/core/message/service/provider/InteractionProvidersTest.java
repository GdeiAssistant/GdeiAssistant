package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.core.express.mapper.ExpressMapper;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.entity.*;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.*;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.entity.TopicLikeEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InteractionProvidersTest {
    private final ObjectMapper json=new ObjectMapper();
    private <T>T fixture(Class<T> type,String fields) throws Exception {return json.readValue("{"+fields+"}",type);}
    private <T>T provider(Class<T> type,String field,Object mapper) throws Exception {
        T value=type.getConstructor().newInstance();ReflectionTestUtils.setField(value,field,mapper);return value;
    }
    @Test void expressMergesThreeSourcesInDescendingTimeAndKeepsNavigationTargets() throws Exception {
        var mapper=mock(ExpressMapper.class);var provider=provider(ExpressInteractionMessageProvider.class,"expressMapper",mapper);
        var comment=fixture(ExpressComment.class,"\"id\":1,\"expressId\":10,\"nickname\":\"合成昵称\",\"username\":\"synthetic\",\"comment\":\"合成评论\",\"publishTime\":1000");
        var like=fixture(ExpressLike.class,"\"id\":2,\"expressId\":10,\"username\":\"synthetic\",\"createTime\":2000");
        var guess=fixture(ExpressGuess.class,"\"id\":3,\"expressId\":10,\"username\":\"synthetic\",\"result\":1,\"createTime\":3000");
        when(mapper.selectReceivedExpressCommentPage("owner",0,2)).thenReturn(List.of(comment));
        when(mapper.selectReceivedExpressLikePage("owner",0,2)).thenReturn(List.of(like));
        when(mapper.selectReceivedExpressGuessPage("owner",0,2)).thenReturn(List.of(guess));
        var records=provider.queryMessages("owner",2);
        assertEquals(List.of("express-guess-3","express-like-2"),records.stream().map(r->r.getMessage().getId()).toList());
        var message=records.get(0).getMessage();assertEquals("10",message.getTargetId());assertEquals("3",message.getTargetSubId());
        assertEquals("guess",message.getTargetType());assertEquals("express",message.getModule());assertTrue(message.getTitle().contains("猜中了"));
        assertTrue(message.getIsRead());assertFalse(message.getCreatedAt().isBlank());
        when(mapper.selectReceivedExpressCommentPage("owner",0,5)).thenReturn(List.of(new ExpressComment(),fixture(ExpressComment.class,"\"id\":4,\"username\":\"合成用户\"")));
        when(mapper.selectReceivedExpressLikePage("owner",0,5)).thenReturn(List.of(new ExpressLike()));
        when(mapper.selectReceivedExpressGuessPage("owner",0,5)).thenReturn(List.of(new ExpressGuess()));
        var fallback=provider.queryMessages("owner",5);assertEquals(4,fallback.size());
        assertTrue(fallback.stream().anyMatch(r->r.getMessage().getContent().contains("有同学")));
        assertTrue(fallback.stream().anyMatch(r->r.getMessage().getContent().contains("合成用户")));
        assertTrue(fallback.stream().anyMatch(r->r.getMessage().getTitle().contains("参与猜名字")));
        assertTrue(provider.queryMessages("",2).isEmpty());assertTrue(provider.queryMessages("owner",0).isEmpty());
        assertTrue(provider.queryMessages("absent",2).isEmpty());
    }
    @Test void photographsAndSecretCommentsKeepContentAndParentIdsWithFallbackSenders() throws Exception {
        var photos=mock(PhotographMapper.class);var photograph=provider(PhotographInteractionMessageProvider.class,"photographMapper",photos);
        var secrets=mock(SecretMapper.class);var secret=provider(SecretInteractionMessageProvider.class,"secretMapper",secrets);
        when(photos.selectReceivedPhotographCommentPage("owner",0,3)).thenReturn(List.of(
                fixture(PhotographCommentEntity.class,"\"commentId\":5,\"photoId\":20,\"nickname\":\"合成昵称\",\"comment\":\"作品评论\",\"createTime\":2000"),new PhotographCommentEntity()));
        when(photos.selectReceivedPhotographLikePage("owner",0,3)).thenReturn(List.of(fixture(PhotographLike.class,"\"likeId\":6,\"photoId\":20,\"username\":\"synthetic\",\"createTime\":1000")));
        var photo=photograph.queryMessages("owner",3).stream().map(InteractionMessageRecord::getMessage).filter(m->"photograph-comment-5".equals(m.getId())).findFirst().orElseThrow();
        assertEquals("20",photo.getTargetId());assertEquals("5",photo.getTargetSubId());assertTrue(photo.getContent().contains("合成昵称"));assertTrue(photo.getContent().contains("作品评论"));
        when(secrets.selectReceivedSecretCommentPage("owner",0,3)).thenReturn(List.of(fixture(SecretCommentEntity.class,"\"id\":7,\"contentId\":30,\"username\":\"synthetic\",\"comment\":\"树洞评论\",\"publishTime\":2000"),new SecretCommentEntity()));
        when(secrets.selectReceivedSecretLikePage("owner",0,3)).thenReturn(List.of(fixture(SecretLikeEntity.class,"\"id\":8,\"contentId\":30,\"username\":\"synthetic\",\"createTime\":1000")));
        var messages=secret.queryMessages("owner",3);var comment=messages.stream().map(InteractionMessageRecord::getMessage).filter(m->"secret-comment-7".equals(m.getId())).findFirst().orElseThrow();
        assertEquals("30",comment.getTargetId());assertEquals("7",comment.getTargetSubId());assertTrue(comment.getContent().contains("树洞评论"));
        for(var p:List.of(photograph,secret)){assertTrue(p.queryMessages("",2).isEmpty());assertTrue(p.queryMessages("owner",0).isEmpty());assertTrue(p.queryMessages("absent",2).isEmpty());}
    }
    @Test void deliveryAndTopicMessagesResolveOrderAndTopicTargets() throws Exception {
        var deliveries=mock(DeliveryMapper.class);var delivery=provider(DeliveryInteractionMessageProvider.class,"deliveryMapper",deliveries);
        when(deliveries.selectPersonalDeliveryInteractionPage("owner",0,3)).thenReturn(List.of(
                fixture(DeliveryTradeEntity.class,"\"tradeId\":9,\"orderId\":40,\"username\":\"synthetic\",\"createTime\":2000,\"deliveryOrder\":{\"orderId\":40,\"pickupLocation\":\"合成取件点\"}"),
                fixture(DeliveryTradeEntity.class,"\"tradeId\":10,\"orderId\":41"),new DeliveryTradeEntity()));
        var messages=delivery.queryMessages("owner",3);assertEquals("40",messages.get(0).getMessage().getTargetId());
        assertEquals("9",messages.get(0).getMessage().getTargetSubId());assertTrue(messages.get(0).getMessage().getContent().contains("合成取件点"));
        assertEquals("41",messages.get(1).getMessage().getTargetId());assertTrue(messages.get(1).getMessage().getContent().contains("快递代收"));
        var topics=mock(TopicMapper.class);var topic=provider(TopicInteractionMessageProvider.class,"topicMapper",topics);
        when(topics.selectReceivedTopicLikePage("owner",0,3)).thenReturn(List.of(fixture(TopicLikeEntity.class,"\"id\":11,\"topicId\":50,\"username\":\"synthetic\",\"createTime\":2000"),new TopicLikeEntity()));
        var message=topic.queryMessages("owner",3).get(0).getMessage();assertEquals("50",message.getTargetId());assertEquals("11",message.getTargetSubId());assertEquals("topic",message.getModule());
        for(var p:List.of(delivery,topic)){assertTrue(p.queryMessages("",2).isEmpty());assertTrue(p.queryMessages("owner",0).isEmpty());assertTrue(p.queryMessages("absent",2).isEmpty());}
    }
}
