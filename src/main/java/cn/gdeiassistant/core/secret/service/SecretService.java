package cn.gdeiassistant.core.secret.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.secret.converter.SecretCommentConverter;
import cn.gdeiassistant.core.secret.converter.SecretConverter;
import cn.gdeiassistant.core.secret.mapper.SecretMapper;
import cn.gdeiassistant.core.secret.pojo.dto.SecretPublishDTO;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class SecretService {

    private static final Logger logger = LoggerFactory.getLogger(SecretService.class);
    private static final int PROFILE_SECRET_LIMIT = 50;
    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "secretMapper")
    private SecretMapper secretMapper;

    @Autowired
    private SecretConverter secretConverter;

    @Autowired
    private SecretCommentConverter secretCommentConverter;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    public List<SecretVO> getSecretInfo(int start, int size, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<SecretContentEntity> list = secretMapper.selectSecretLight(start, size);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        list.forEach(e -> e.setUsername(AnonymizeUtils.sanitizeUsername(e.getUsername())));
        List<SecretVO> result = secretConverter.toVOList(list);
        enrichVOsWithBatchCounts(result, list, user.getUsername());
        return result;
    }

    public List<SecretVO> getSecretInfo(String sessionId) throws Exception {
        return getSecretInfo(sessionId, 0, PROFILE_SECRET_LIMIT);
    }

    public List<SecretVO> getSecretInfo(String sessionId, int start, int size) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<SecretContentEntity> list = secretMapper.selectSecretByUsernameLight(user.getUsername(), start, size);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        list.forEach(e -> e.setUsername(AnonymizeUtils.sanitizeUsername(e.getUsername())));
        List<SecretVO> result = secretConverter.toVOList(list);
        enrichVOsWithBatchCounts(result, list, user.getUsername());
        return result;
    }

    public List<SecretCommentVO> getSecretComments(int contentId) throws Exception {
        List<SecretCommentEntity> list = secretMapper.selectSecretCommentsByContentId(contentId);
        if (list == null) return new ArrayList<>();
        list.forEach(e -> e.setUsername(AnonymizeUtils.sanitizeUsername(e.getUsername())));
        return secretCommentConverter.toVOList(list);
    }

    public boolean checkSecretInfoExist(int id) throws Exception {
        SecretContentEntity entity = secretMapper.selectSecretByID(id);
        return entity != null;
    }

    public String getSecretVoiceURL(int id) {
        String voiceObjectKey = findSecretVoiceObjectKey(id);
        if (voiceObjectKey == null) {
            return "";
        }
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", voiceObjectKey, 30, TimeUnit.MINUTES);
    }

    public void uploadVoiceSecret(int id, InputStream inputStream) throws RuntimeException {
        try {
            r2StorageService.uploadObject("gdeiassistant-userdata", "secret/voice/" + id + ".mp3", inputStream);
        } catch (Exception e) {
            logger.error("上传树洞语音失败，id={}", id, e);
            throw new RuntimeException("语音上传失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭树洞语音上传输入流失败，id={}", id, e);
                }
            }
        }
    }

    public void moveVoiceSecretFromTempObject(int id, String objectKey) {
        String extension = extractExtension(objectKey);
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "secret/voice/" + id + extension);
    }

    public String findSecretVoiceObjectKey(int id) {
        String[] candidates = new String[]{
                "secret/voice/" + id + ".mp3",
                "secret/voice/" + id + ".webm",
                "secret/voice/" + id + ".ogg",
                "secret/voice/" + id + ".wav",
                "secret/voice/" + id + ".m4a",
                "secret/voice/" + id + ".mp4"
        };
        for (String candidate : candidates) {
            String url = r2StorageService.generatePresignedUrl("gdeiassistant-userdata", candidate, 1, TimeUnit.MINUTES);
            if (url != null && !url.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

    public SecretVO getSecretDetailInfo(int id, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        SecretContentEntity entity = secretMapper.selectSecretByID(id);
        if (entity == null) {
            throw new DataNotExistException("查询的树洞消息不存在");
        }
        entity.setUsername(AnonymizeUtils.sanitizeUsername(entity.getUsername()));
        SecretVO vo = secretConverter.toVO(entity);
        if (entity.getType() != null && entity.getType() == 1) {
            vo.setVoiceURL(getSecretVoiceURL(entity.getId()));
        }
        vo.setCommentCount(secretMapper.selectSecretCommentCount(entity.getId()));
        vo.setLikeCount(secretMapper.selectSecretLikeCount(entity.getId()));
        vo.setLiked(secretMapper.selectSecretLike(entity.getId(), user.getUsername()));
        return vo;
    }

    public Integer addSecretInfo(String sessionId, SecretPublishDTO dto) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        SecretContentEntity entity = dtoToEntity(dto, user.getUsername());
        secretMapper.insertSecret(entity);
        return entity.getId();
    }

    public void deleteSecretById(int id) {
        secretMapper.deleteSecret(id);
    }

    public void deleteSecretVoice(int id) {
        String[] extensions = new String[]{".mp3", ".webm", ".ogg", ".wav", ".m4a", ".mp4"};
        for (String ext : extensions) {
            try {
                r2StorageService.deleteObject("gdeiassistant-userdata", "secret/voice/" + id + ext);
            } catch (Exception e) {
                logger.warn("删除树洞语音失败，id={}，extension={}", id, ext, e);
            }
        }
    }

    @Transactional("appTransactionManager")
    public void addSecretComment(int id, String sessionId, String comment) throws Exception {
        if (comment == null || comment.trim().isEmpty() || comment.length() > 50) {
            throw new IllegalArgumentException("评论内容不能为空且不能超过 50 字");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        SecretCommentEntity entity = new SecretCommentEntity();
        entity.setContentId(id);
        entity.setUsername(user.getUsername());
        entity.setComment(comment);
        entity.setAvatarTheme((int) (Math.random() * 50));
        secretMapper.insertSecretComment(entity);
        SecretContentEntity contentEntity = secretMapper.selectSecretByID(id);
        interactionNotificationService.createInteractionNotification(
                "secret",
                "comment",
                contentEntity != null ? contentEntity.getUsername() : null,
                user.getUsername(),
                String.valueOf(id),
                entity.getId() == null ? null : String.valueOf(entity.getId()),
                "comment",
                "树洞收到新评论",
                user.getUsername() + " 评论了你的树洞：" + comment
        );
    }

    @Transactional("appTransactionManager")
    public void changeUserLikeState(boolean like, int id, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (like) {
            Integer existingLikeCount = secretMapper.selectSecretLike(id, user.getUsername());
            if (existingLikeCount == null || existingLikeCount == 0) {
                secretMapper.insertSecretLike(id, user.getUsername());
                SecretContentEntity contentEntity = secretMapper.selectSecretByID(id);
                interactionNotificationService.createInteractionNotification(
                        "secret",
                        "like",
                        contentEntity != null ? contentEntity.getUsername() : null,
                        user.getUsername(),
                        String.valueOf(id),
                        null,
                        "like",
                        "树洞收到新点赞",
                        user.getUsername() + " 点赞了你的树洞"
                );
            }
        } else {
            secretMapper.deleteSecretLike(id, user.getUsername());
        }
    }

    /**
     * 定时清理树洞内容（可由 Scheduler 触发）。
     */
    @Transactional("appTransactionManager")
    public void deleteTimerSecretInfos() throws Exception {
        List<SecretContentEntity> list = secretMapper.selectNotRemovedSecrets();
        for (SecretContentEntity entity : list) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time = LocalDateTime.ofInstant(entity.getPublishTime().toInstant(), ZoneId.systemDefault());
            if (ChronoUnit.HOURS.between(time, now) >= 24) {
                secretMapper.deleteSecret(entity.getId());
            }
        }
    }

    /**
     * Batch-enrich a list of SecretVOs with commentCount, likeCount, and liked fields.
     * Replaces the previous per-item N+1 queries with 3 batch queries.
     */
    private void enrichVOsWithBatchCounts(List<SecretVO> vos, List<SecretContentEntity> entities, String username) {
        List<Integer> contentIds = entities.stream()
                .map(SecretContentEntity::getId)
                .collect(Collectors.toList());
        if (contentIds.isEmpty()) {
            return;
        }

        // Batch comment counts
        Map<Integer, Integer> commentCounts = new HashMap<>();
        List<Map<String, Object>> commentRows = secretMapper.selectSecretCommentCounts(contentIds);
        if (commentRows != null) {
            for (Map<String, Object> row : commentRows) {
                Integer contentId = ((Number) row.get("content_id")).intValue();
                Integer cnt = ((Number) row.get("cnt")).intValue();
                commentCounts.put(contentId, cnt);
            }
        }

        // Batch like counts
        Map<Integer, Integer> likeCounts = new HashMap<>();
        List<Map<String, Object>> likeRows = secretMapper.selectSecretLikeCounts(contentIds);
        if (likeRows != null) {
            for (Map<String, Object> row : likeRows) {
                Integer contentId = ((Number) row.get("content_id")).intValue();
                Integer cnt = ((Number) row.get("cnt")).intValue();
                likeCounts.put(contentId, cnt);
            }
        }

        // Batch liked state
        Set<Integer> likedIds = new HashSet<>();
        List<Integer> likedList = secretMapper.selectLikedSecretContentIds(username, contentIds);
        if (likedList != null) {
            likedIds.addAll(likedList);
        }

        // Map onto VOs
        for (int i = 0; i < vos.size(); i++) {
            SecretVO vo = vos.get(i);
            Integer id = entities.get(i).getId();
            vo.setCommentCount(commentCounts.getOrDefault(id, 0));
            vo.setLikeCount(likeCounts.getOrDefault(id, 0));
            vo.setLiked(likedIds.contains(id) ? 1 : 0);
        }
    }

    private SecretContentEntity dtoToEntity(SecretPublishDTO dto, String username) {
        SecretContentEntity e = new SecretContentEntity();
        e.setUsername(username);
        e.setTheme(dto.getTheme());
        e.setContent(dto.getContent());
        e.setType(dto.getType());
        e.setTimer(dto.getTimer());
        return e;
    }

    private String extractExtension(String objectKey) {
        if (objectKey == null) {
            return ".mp3";
        }
        int dotIndex = objectKey.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == objectKey.length() - 1) {
            return ".mp3";
        }
        String extension = objectKey.substring(dotIndex).toLowerCase();
        return extension.matches("\\.[a-z0-9]{1,10}") ? extension : ".mp3";
    }
}
