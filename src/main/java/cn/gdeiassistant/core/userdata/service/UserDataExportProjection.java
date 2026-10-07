package cn.gdeiassistant.core.userdata.service;

import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.cetquery.pojo.entity.CetNumberEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.common.pojo.entity.ChargeLog;

import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit export fields: newly added entity fields do not become downloadable automatically. */
final class UserDataExportProjection {
    private UserDataExportProjection() {}

    private static void put(Map<String, Object> result, String key, Object value) {
        if (value != null) result.put(key, value);
    }

    static Map<String, Object> phone(PhoneEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "username", value.getUsername());
        put(result, "code", value.getCode());
        put(result, "phone", value.getPhone());
        put(result, "createTime", value.getCreateTime());
        put(result, "updateTime", value.getUpdateTime());
        return result;
    }

    static Map<String, Object> profile(ProfileEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "username", value.getUsername());
        put(result, "nickname", value.getNickname());
        put(result, "birthday", value.getBirthday());
        put(result, "degree", value.getDegree());
        put(result, "faculty", value.getFaculty());
        put(result, "major", value.getMajor());
        put(result, "enrollment", value.getEnrollment());
        put(result, "profession", value.getProfession());
        put(result, "colleges", value.getColleges());
        put(result, "highSchool", value.getHighSchool());
        put(result, "juniorHighSchool", value.getJuniorHighSchool());
        put(result, "primarySchool", value.getPrimarySchool());
        put(result, "locationRegion", value.getLocationRegion());
        put(result, "locationState", value.getLocationState());
        put(result, "locationCity", value.getLocationCity());
        put(result, "hometownRegion", value.getHometownRegion());
        put(result, "hometownState", value.getHometownState());
        put(result, "hometownCity", value.getHometownCity());
        return result;
    }

    static Map<String, Object> privacy(PrivacyEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "username", value.getUsername());
        put(result, "locationOpen", value.getLocationOpen());
        put(result, "hometownOpen", value.getHometownOpen());
        put(result, "introductionOpen", value.getIntroductionOpen());
        put(result, "facultyOpen", value.getFacultyOpen());
        put(result, "majorOpen", value.getMajorOpen());
        put(result, "enrollmentOpen", value.getEnrollmentOpen());
        put(result, "ageOpen", value.getAgeOpen());
        put(result, "cacheAllow", value.getCacheAllow());
        put(result, "quickAuthAllow", value.getQuickAuthAllow());
        put(result, "robotsIndexAllow", value.getRobotsIndexAllow());
        put(result, "dmPolicy", value.getDmPolicy());
        return result;
    }

    static Map<String, Object> cet(CetNumberEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "username", value.getUsername());
        put(result, "number", value.getNumber());
        return result;
    }

    static Map<String, Object> deliveryOrder(DeliveryOrderEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "orderId", value.getOrderId());
        put(result, "username", value.getUsername());
        put(result, "orderTime", value.getOrderTime());
        put(result, "taskName", value.getTaskName());
        put(result, "pickupCode", value.getPickupCode());
        put(result, "contactPhone", value.getContactPhone());
        put(result, "price", value.getPrice());
        put(result, "pickupLocation", value.getPickupLocation());
        put(result, "deliveryAddress", value.getDeliveryAddress());
        put(result, "state", value.getState());
        put(result, "remarks", value.getRemarks());
        return result;
    }

    static Map<String, Object> deliveryTrade(DeliveryTradeEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "tradeId", value.getTradeId());
        put(result, "orderId", value.getOrderId());
        put(result, "createTime", value.getCreateTime());
        put(result, "username", value.getUsername());
        put(result, "state", value.getState());
        put(result, "deliveryOrder", value.getDeliveryOrder() == null ? null : deliveryOrder(value.getDeliveryOrder()));
        return result;
    }

    static Map<String, Object> marketplaceItem(MarketplaceItemEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "username", value.getUsername());
        put(result, "authorId", value.getAuthorId());
        put(result, "displayName", value.getDisplayName());
        put(result, "name", value.getName());
        put(result, "description", value.getDescription());
        put(result, "price", value.getPrice());
        put(result, "location", value.getLocation());
        put(result, "type", value.getType());
        put(result, "qq", value.getQq());
        put(result, "phone", value.getPhone());
        put(result, "state", value.getState());
        put(result, "publishTime", value.getPublishTime());
        put(result, "pictureURL", value.getPictureURL());
        return result;
    }

    static Map<String, Object> lostAndFoundItem(LostAndFoundItemEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "username", value.getUsername());
        put(result, "name", value.getName());
        put(result, "description", value.getDescription());
        put(result, "location", value.getLocation());
        put(result, "itemType", value.getItemType());
        put(result, "lostType", value.getLostType());
        put(result, "qq", value.getQq());
        put(result, "wechat", value.getWechat());
        put(result, "phone", value.getPhone());
        put(result, "state", value.getState());
        put(result, "publishTime", value.getPublishTime());
        put(result, "pictureURL", value.getPictureURL());
        return result;
    }

    static Map<String, Object> secretContent(SecretContentEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "username", value.getUsername());
        put(result, "content", value.getContent());
        put(result, "theme", value.getTheme());
        put(result, "type", value.getType());
        put(result, "timer", value.getTimer());
        put(result, "state", value.getState());
        put(result, "publishTime", value.getPublishTime());
        put(result, "likeCount", value.getLikeCount());
        put(result, "commentCount", value.getCommentCount());
        put(result, "secretCommentList", value.getSecretCommentList() == null ? null : value.getSecretCommentList().stream().map(UserDataExportProjection::secretComment).toList());
        put(result, "voiceURL", value.getVoiceURL());
        return result;
    }

    static Map<String, Object> secretComment(SecretCommentEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "contentId", value.getContentId());
        put(result, "comment", value.getComment());
        put(result, "publishTime", value.getPublishTime());
        put(result, "avatarTheme", value.getAvatarTheme());
        return result;
    }

    static Map<String, Object> photograph(PhotographEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "title", value.getTitle());
        put(result, "content", value.getContent());
        put(result, "count", value.getCount());
        put(result, "type", value.getType());
        put(result, "username", value.getUsername());
        put(result, "createTime", value.getCreateTime());
        put(result, "likeCount", value.getLikeCount());
        put(result, "commentCount", value.getCommentCount());
        put(result, "liked", value.getLiked());
        put(result, "photographCommentList", value.getPhotographCommentList() == null ? null : value.getPhotographCommentList().stream().map(UserDataExportProjection::photographComment).toList());
        return result;
    }

    static Map<String, Object> photographComment(PhotographCommentEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "commentId", value.getCommentId());
        put(result, "photoId", value.getPhotoId());
        put(result, "nickname", value.getNickname());
        put(result, "comment", value.getComment());
        put(result, "createTime", value.getCreateTime());
        return result;
    }

    static Map<String, Object> express(ExpressEntity value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "id", value.getId());
        put(result, "username", value.getUsername());
        put(result, "nickname", value.getNickname());
        put(result, "realname", value.getRealname());
        put(result, "selfGender", value.getSelfGender());
        put(result, "name", value.getName());
        put(result, "content", value.getContent());
        put(result, "personGender", value.getPersonGender());
        put(result, "publishTime", value.getPublishTime());
        put(result, "likeCount", value.getLikeCount());
        put(result, "liked", value.getLiked());
        put(result, "commentCount", value.getCommentCount());
        put(result, "guessCount", value.getGuessCount());
        put(result, "guessSum", value.getGuessSum());
        put(result, "canGuess", value.getCanGuess());
        return result;
    }

    static Map<String, Object> chargeLog(ChargeLog value) {
        Map<String, Object> result = new LinkedHashMap<>();
        put(result, "username", value.getUsername());
        put(result, "amount", value.getAmount());
        return result;
    }
}
