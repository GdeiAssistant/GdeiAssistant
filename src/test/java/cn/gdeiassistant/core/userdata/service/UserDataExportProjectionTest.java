package cn.gdeiassistant.core.userdata.service;

import cn.gdeiassistant.core.delivery.pojo.entity.*;
import cn.gdeiassistant.core.photograph.pojo.entity.*;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.secret.pojo.entity.*;
import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class UserDataExportProjectionTest {
    static class ExtendedProfile extends ProfileEntity {
        private final String internalCredential="synthetic-private-value";
        public String getInternalCredential(){return internalCredential;}
    }
    static class ExtendedComment extends SecretCommentEntity {
        public String internalToken="synthetic-internal-token";
    }
    @Test void newlyAddedAndInheritedInternalFieldsNeverEnterExport() {
        var profile=new ExtendedProfile();profile.setUsername("synthetic-owner");profile.setNickname("Synthetic");profile.setDegree(0);
        var exported=UserDataExportProjection.profile(profile);
        assertEquals(Map.of("username","synthetic-owner","nickname","Synthetic","degree",0),exported);
        assertFalse(JSON.toJSONString(exported).contains("synthetic-private-value"));
        profile.setNickname(null);assertFalse(UserDataExportProjection.profile(profile).containsKey("nickname"));
    }
    @Test void nestedOrderIsProjectedInsteadOfPassingAnEntityToTheSerializer() {
        var order=new DeliveryOrderEntity();order.setOrderId(7);order.setPrice(new BigDecimal("1.20"));order.setState(0);
        var trade=new DeliveryTradeEntity();trade.setTradeId(8);trade.setDeliveryOrder(order);
        var exported=UserDataExportProjection.deliveryTrade(trade);
        assertInstanceOf(Map.class,exported.get("deliveryOrder"));
        assertEquals(Map.of("orderId",7,"price",new BigDecimal("1.20"),"state",0),exported.get("deliveryOrder"));
    }
    @Test void nestedCommentsRetainContentAndDatesWithoutOtherUsersIdentityOrInternalFields() {
        var date=new Date(1000);
        var comment=new ExtendedComment();comment.setId(3);comment.setContentId(2);comment.setComment("synthetic text");comment.setUsername("synthetic-other-person");comment.setPublishTime(date);
        var secret=new SecretContentEntity();secret.setId(2);secret.setSecretCommentList(List.of(comment));
        var data=UserDataExportProjection.secretContent(secret);
        assertEquals(List.of(Map.of("id",3,"contentId",2,"comment","synthetic text","publishTime",date)),data.get("secretCommentList"));
        String json=JSON.toJSONString(data);assertFalse(json.contains("synthetic-other-person"));assertFalse(json.contains("synthetic-internal-token"));
        var photoComment=new PhotographCommentEntity();photoComment.setCommentId(4);photoComment.setPhotoId(5);photoComment.setUsername("synthetic-other-person");photoComment.setNickname("Display");photoComment.setComment("photo text");
        var photo=new PhotographEntity();photo.setId(5);photo.setPhotographCommentList(List.of(photoComment));
        var photoData=UserDataExportProjection.photograph(photo);
        assertEquals(List.of(Map.of("commentId",4,"photoId",5,"nickname","Display","comment","photo text")),photoData.get("photographCommentList"));
        assertFalse(JSON.toJSONString(photoData).contains("synthetic-other-person"));
    }
}
