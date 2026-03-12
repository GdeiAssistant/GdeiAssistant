package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.message.pojo.entity.InteractionNotificationEntity;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.roommate.mapper.RoommateMapper;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateMessageEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommatePickEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateProfileEntity;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class InteractionNotificationService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String LEGACY_DATING_ID_PREFIX = "dating-";
    private static final int DEFAULT_MERGED_QUERY_WINDOW = 32;
    private static final int MAX_MERGED_QUERY_WINDOW = 512;

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "interactionNotificationMapper")
    private InteractionNotificationMapper interactionNotificationMapper;

    @Resource(name = "roommateMapper")
    private RoommateMapper roommateMapper;

    public void createInteractionNotification(String module, String type, String receiverUsername, String actorUsername,
            String targetId, String targetSubId, String targetType, String title, String content) {
        if (StringUtils.isBlank(module) || StringUtils.isBlank(type) || StringUtils.isBlank(receiverUsername)) {
            return;
        }
        if (StringUtils.isNotBlank(actorUsername) && receiverUsername.equals(actorUsername)) {
            return;
        }
        InteractionNotificationEntity entity = new InteractionNotificationEntity();
        entity.setModule(normalizeModule(module));
        entity.setType(type);
        entity.setReceiverUsername(receiverUsername);
        entity.setActorUsername(actorUsername);
        entity.setTargetId(targetId);
        entity.setTargetSubId(targetSubId);
        entity.setTargetType(targetType);
        entity.setTitle(StringUtils.isNotBlank(title) ? title : "互动消息");
        entity.setContent(StringUtils.isNotBlank(content) ? content : "你有一条新的互动消息");
        entity.setIsRead(0);
        interactionNotificationMapper.insertInteractionNotification(entity);
    }

    public List<InteractionMessageVO> queryInteractionMessages(String sessionId, Integer start, Integer size) {
        return queryInteractionMessages(sessionId, start, size, false);
    }

    public List<InteractionMessageVO> queryInteractionMessages(String sessionId, Integer start, Integer size,
            boolean includeLegacyDating) {
        if (start == null || start < 0 || size == null || size <= 0) {
            return new ArrayList<>();
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (includeLegacyDating) {
            return queryMergedInteractionMessages(user.getUsername(), start, size);
        }
        List<InteractionNotificationEntity> entityList = interactionNotificationMapper
                .selectInteractionNotificationPage(user.getUsername(), start, size);
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }
        List<InteractionMessageVO> list = new ArrayList<>(entityList.size());
        for (InteractionNotificationEntity entity : entityList) {
            list.add(toInteractionMessageVO(entity));
        }
        return list;
    }

    public Integer queryInteractionUnreadCount(String sessionId) {
        return queryInteractionUnreadCount(sessionId, false);
    }

    public Integer queryInteractionUnreadCount(String sessionId, boolean includeLegacyDating) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer count = interactionNotificationMapper.selectUnreadInteractionNotificationCount(user.getUsername());
        int unreadCount = count == null ? 0 : Math.max(count, 0);
        if (includeLegacyDating) {
            unreadCount += queryLegacyDatingUnreadCount(user.getUsername());
        }
        return unreadCount;
    }

    public void markInteractionNotificationRead(String sessionId, String notificationId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer legacyDatingMessageId = parseLegacyDatingMessageId(notificationId);
        if (legacyDatingMessageId != null) {
            roommateMapper.updateRoommateMessageStateByUsername(legacyDatingMessageId, 1, user.getUsername());
            return;
        }
        Long parsedId = parseNotificationId(notificationId);
        if (parsedId == null) {
            return;
        }
        interactionNotificationMapper.updateInteractionNotificationRead(user.getUsername(), parsedId);
    }

    public void markAllInteractionNotificationsRead(String sessionId) {
        markAllInteractionNotificationsRead(sessionId, false);
    }

    public void markAllInteractionNotificationsRead(String sessionId, boolean includeLegacyDating) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        interactionNotificationMapper.updateAllInteractionNotificationRead(user.getUsername());
        if (includeLegacyDating) {
            roommateMapper.updateAllRoommateMessageStateByUsername(user.getUsername(), 1);
        }
    }

    private InteractionMessageVO toInteractionMessageVO(InteractionNotificationEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        vo.setId(entity.getNotificationId() == null ? null : String.valueOf(entity.getNotificationId()));
        vo.setModule(normalizeModule(entity.getModule()));
        vo.setType(entity.getType());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setCreatedAt(formatDate(entity.getCreateTime()));
        vo.setIsRead(entity.getIsRead() != null && entity.getIsRead() != 0);
        vo.setTargetType(entity.getTargetType());
        vo.setTargetId(entity.getTargetId());
        vo.setTargetSubId(entity.getTargetSubId());
        return vo;
    }

    private List<InteractionMessageVO> queryMergedInteractionMessages(String username, int start, int size) {
        int expectedCount = start + size;
        int queryWindow = Math.max(DEFAULT_MERGED_QUERY_WINDOW, expectedCount);
        List<InteractionFeedRow> mergedRows = new ArrayList<>();

        while (true) {
            List<InteractionNotificationEntity> notificationList = interactionNotificationMapper
                    .selectInteractionNotificationPage(username, 0, queryWindow);
            List<RoommateMessageEntity> legacyDatingList = roommateMapper
                    .selectUserRoommateMessageInteractionPage(username, 0, queryWindow);

            mergedRows = mergeInteractionRows(notificationList, legacyDatingList);

            boolean notificationExhausted = notificationList == null || notificationList.size() < queryWindow;
            boolean legacyExhausted = legacyDatingList == null || legacyDatingList.size() < queryWindow;
            if (mergedRows.size() >= expectedCount || (notificationExhausted && legacyExhausted)
                    || queryWindow >= MAX_MERGED_QUERY_WINDOW) {
                break;
            }

            queryWindow = Math.min(queryWindow * 2, MAX_MERGED_QUERY_WINDOW);
        }

        if (start >= mergedRows.size()) {
            return new ArrayList<>();
        }

        int endIndex = Math.min(start + size, mergedRows.size());
        List<InteractionMessageVO> result = new ArrayList<>(endIndex - start);
        for (int index = start; index < endIndex; index++) {
            result.add(mergedRows.get(index).message);
        }
        return result;
    }

    private List<InteractionFeedRow> mergeInteractionRows(List<InteractionNotificationEntity> notificationList,
            List<RoommateMessageEntity> legacyDatingList) {
        List<InteractionFeedRow> mergedRows = new ArrayList<>();
        Set<String> datingKeys = new HashSet<>();

        if (notificationList != null) {
            for (InteractionNotificationEntity entity : notificationList) {
                mergedRows.add(new InteractionFeedRow(toInteractionMessageVO(entity), entity.getCreateTime(),
                        entity.getNotificationId() == null ? 0L : entity.getNotificationId()));
                if ("dating".equals(normalizeModule(entity.getModule()))) {
                    datingKeys.add(buildDatingInteractionKey(entity.getTargetId(), entity.getTargetSubId(),
                            entity.getTargetType()));
                }
            }
        }

        if (legacyDatingList != null) {
            for (RoommateMessageEntity entity : legacyDatingList) {
                String key = buildDatingInteractionKey(entity);
                if (datingKeys.contains(key)) {
                    continue;
                }
                mergedRows.add(toLegacyDatingInteractionRow(entity));
            }
        }

        mergedRows.sort((left, right) -> {
            Date leftDate = left.createTime;
            Date rightDate = right.createTime;
            if (leftDate == null && rightDate == null) {
                return Long.compare(right.sortOrder, left.sortOrder);
            }
            if (leftDate == null) {
                return 1;
            }
            if (rightDate == null) {
                return -1;
            }
            int dateCompare = rightDate.compareTo(leftDate);
            return dateCompare != 0 ? dateCompare : Long.compare(right.sortOrder, left.sortOrder);
        });
        return mergedRows;
    }

    private int queryLegacyDatingUnreadCount(String username) {
        List<RoommateMessageEntity> unreadLegacyDatingList = roommateMapper
                .selectUnreadUserRoommateMessageInteractionList(username);
        if (unreadLegacyDatingList == null || unreadLegacyDatingList.isEmpty()) {
            return 0;
        }

        List<InteractionNotificationEntity> notificationKeyList = interactionNotificationMapper
                .selectDatingInteractionNotificationKeys(username);
        Set<String> datingKeys = new HashSet<>();
        if (notificationKeyList != null) {
            for (InteractionNotificationEntity entity : notificationKeyList) {
                datingKeys.add(buildDatingInteractionKey(entity.getTargetId(), entity.getTargetSubId(),
                        entity.getTargetType()));
            }
        }

        int unreadCount = 0;
        for (RoommateMessageEntity entity : unreadLegacyDatingList) {
            if (!datingKeys.contains(buildDatingInteractionKey(entity))) {
                unreadCount++;
            }
        }
        return unreadCount;
    }

    private InteractionFeedRow toLegacyDatingInteractionRow(RoommateMessageEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        RoommatePickEntity pick = entity.getRoommatePick();
        RoommateProfileEntity profile = pick == null ? null : pick.getRoommateProfile();

        boolean received = Integer.valueOf(0).equals(entity.getType());
        boolean accepted = pick != null && Integer.valueOf(1).equals(pick.getState());
        boolean rejected = pick != null && Integer.valueOf(-1).equals(pick.getState());

        vo.setId(entity.getMessageId() == null ? null : LEGACY_DATING_ID_PREFIX + entity.getMessageId());
        vo.setModule("dating");
        vo.setType(resolveLegacyDatingType(received, accepted, rejected));
        vo.setTitle(resolveLegacyDatingTitle(received, accepted, rejected));
        vo.setContent(resolveLegacyDatingContent(pick, profile, received, accepted, rejected));
        vo.setCreatedAt(formatDate(entity.getCreateTime()));
        vo.setIsRead(entity.getState() != null && entity.getState() != 0);
        vo.setTargetType(received ? "received" : "sent");
        vo.setTargetId(pick == null || pick.getPickId() == null ? null : String.valueOf(pick.getPickId()));
        vo.setTargetSubId(profile == null || profile.getProfileId() == null ? null : String.valueOf(profile.getProfileId()));

        long sortOrder = entity.getMessageId() == null ? 0L : entity.getMessageId();
        return new InteractionFeedRow(vo, entity.getCreateTime(), sortOrder);
    }

    private String resolveLegacyDatingType(boolean received, boolean accepted, boolean rejected) {
        if (received) {
            return "pick_received";
        }
        if (accepted) {
            return "pick_accepted";
        }
        if (rejected) {
            return "pick_rejected";
        }
        return "pick_updated";
    }

    private String resolveLegacyDatingTitle(boolean received, boolean accepted, boolean rejected) {
        if (received) {
            return "收到新的撩一下";
        }
        if (accepted) {
            return "撩一下已通过";
        }
        if (rejected) {
            return "撩一下未通过";
        }
        return "撩一下状态已更新";
    }

    private String resolveLegacyDatingContent(RoommatePickEntity pick, RoommateProfileEntity profile, boolean received,
            boolean accepted, boolean rejected) {
        if (received) {
            String actorName = pick != null && StringUtils.isNotBlank(pick.getUsername()) ? pick.getUsername() : "有人";
            if (pick != null && StringUtils.isNotBlank(pick.getContent())) {
                return actorName + " 向你发起了撩一下：" + pick.getContent();
            }
            return actorName + " 向你发起了撩一下";
        }

        String nickname = profile != null && StringUtils.isNotBlank(profile.getNickname()) ? profile.getNickname() : "对方";
        if (accepted) {
            return nickname + " 通过了你的请求";
        }
        if (rejected) {
            return nickname + " 拒绝了你的请求";
        }
        return nickname + " 更新了你的请求状态";
    }

    private String formatDate(java.util.Date value) {
        return value == null ? "" : value.toInstant().atZone(ZONE_ID).toLocalDateTime().format(DATETIME_FORMATTER);
    }

    private String normalizeModule(String module) {
        if (StringUtils.isBlank(module)) {
            return module;
        }
        switch (module.trim()) {
            case "roommate":
                return "dating";
            case "ershou":
            case "secondhand":
                return "marketplace";
            case "lost_found":
            case "lostfound":
                return "lostandfound";
            default:
                return module.trim();
        }
    }

    private Long parseNotificationId(String notificationId) {
        if (StringUtils.isBlank(notificationId) || !StringUtils.isNumeric(notificationId)) {
            return null;
        }
        try {
            return Long.valueOf(notificationId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseLegacyDatingMessageId(String notificationId) {
        if (StringUtils.isBlank(notificationId) || !notificationId.startsWith(LEGACY_DATING_ID_PREFIX)) {
            return null;
        }

        String rawId = notificationId.substring(LEGACY_DATING_ID_PREFIX.length());
        if (!StringUtils.isNumeric(rawId)) {
            return null;
        }

        try {
            return Integer.valueOf(rawId);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String buildDatingInteractionKey(RoommateMessageEntity entity) {
        RoommatePickEntity pick = entity.getRoommatePick();
        RoommateProfileEntity profile = pick == null ? null : pick.getRoommateProfile();
        String targetId = pick == null || pick.getPickId() == null ? null : String.valueOf(pick.getPickId());
        String targetSubId = profile == null || profile.getProfileId() == null ? null : String.valueOf(profile.getProfileId());
        String targetType = Integer.valueOf(0).equals(entity.getType()) ? "received" : "sent";
        return buildDatingInteractionKey(targetId, targetSubId, targetType);
    }

    private String buildDatingInteractionKey(String targetId, String targetSubId, String targetType) {
        return normalizeKeyPart(targetId) + "|" + normalizeKeyPart(targetSubId) + "|" + normalizeKeyPart(targetType);
    }

    private String normalizeKeyPart(String value) {
        return StringUtils.isBlank(value) ? "" : value.trim();
    }

    private static class InteractionFeedRow {

        private final InteractionMessageVO message;
        private final Date createTime;
        private final long sortOrder;

        private InteractionFeedRow(InteractionMessageVO message, Date createTime, long sortOrder) {
            this.message = message;
            this.createTime = createTime;
            this.sortOrder = sortOrder;
        }
    }
}
