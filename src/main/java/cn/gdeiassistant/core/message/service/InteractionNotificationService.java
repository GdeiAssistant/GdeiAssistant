package cn.gdeiassistant.core.message.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.message.mapper.InteractionNotificationMapper;
import cn.gdeiassistant.core.message.pojo.entity.InteractionNotificationEntity;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class InteractionNotificationService {

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final ZoneId ZONE_ID = ZoneId.of("Asia/Shanghai");

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "interactionNotificationMapper")
    private InteractionNotificationMapper interactionNotificationMapper;

    @Autowired(required = false)
    private UserMapper userMapper;

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
        if (start == null || start < 0 || size == null || size <= 0) {
            return new ArrayList<>();
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<InteractionNotificationEntity> entityList = interactionNotificationMapper
                .selectInteractionNotificationPage(user.getUsername(), start, size);
        if (entityList == null || entityList.isEmpty()) {
            return new ArrayList<>();
        }
        // Batch check which actor usernames are deleted (pre-fix historical data)
        java.util.Set<String> deletedActors = new java.util.HashSet<>();
        if (userMapper != null) {
            java.util.Set<String> uniqueActors = new java.util.HashSet<>();
            for (InteractionNotificationEntity e : entityList) {
                if (e.getActorUsername() != null && !e.getActorUsername().startsWith("del_")) {
                    uniqueActors.add(e.getActorUsername());
                }
            }
            for (String actor : uniqueActors) {
                if (userMapper.selectUser(actor) == null) {
                    deletedActors.add(actor);
                }
            }
        }
        List<InteractionMessageVO> list = new ArrayList<>(entityList.size());
        for (InteractionNotificationEntity entity : entityList) {
            String originalActor = entity.getActorUsername();
            if (deletedActors.contains(originalActor)) {
                // Replace original username in content, then mark actor as anonymous
                if (entity.getContent() != null) {
                    entity.setContent(entity.getContent().replace(originalActor, AnonymizeUtils.sanitizeUsername("del_")));
                }
                entity.setActorUsername("del_legacy");
            }
            list.add(toInteractionMessageVO(entity));
        }
        return list;
    }

    public Integer queryInteractionUnreadCount(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer count = interactionNotificationMapper.selectUnreadInteractionNotificationCount(user.getUsername());
        return count == null ? 0 : Math.max(count, 0);
    }

    public void markInteractionNotificationRead(String sessionId, String notificationId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Long parsedId = parseNotificationId(notificationId);
        if (parsedId == null) {
            return;
        }
        interactionNotificationMapper.updateInteractionNotificationRead(user.getUsername(), parsedId);
    }

    public void markAllInteractionNotificationsRead(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        interactionNotificationMapper.updateAllInteractionNotificationRead(user.getUsername());
    }

    private InteractionMessageVO toInteractionMessageVO(InteractionNotificationEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        vo.setId(entity.getNotificationId() == null ? null : String.valueOf(entity.getNotificationId()));
        vo.setModule(normalizeModule(entity.getModule()));
        vo.setType(entity.getType());
        vo.setTitle(entity.getTitle());
        vo.setContent(anonymizeContent(entity.getContent(), entity.getActorUsername()));
        vo.setCreatedAt(formatDate(entity.getCreateTime()));
        vo.setIsRead(entity.getIsRead() != null && entity.getIsRead() != 0);
        vo.setTargetType(entity.getTargetType());
        vo.setTargetId(entity.getTargetId());
        vo.setTargetSubId(entity.getTargetSubId());
        return vo;
    }

    private String formatDate(java.util.Date value) {
        return value == null ? "" : value.toInstant().atZone(ZONE_ID).toLocalDateTime().format(DATETIME_FORMATTER);
    }

    private String anonymizeContent(String content, String actorUsername) {
        if (content != null && actorUsername != null && actorUsername.startsWith("del_")) {
            return content.replace(actorUsername, AnonymizeUtils.sanitizeUsername(actorUsername));
        }
        return content;
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
}
