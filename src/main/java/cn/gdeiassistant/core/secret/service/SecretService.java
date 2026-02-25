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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SecretService {

    private static final Logger logger = LoggerFactory.getLogger(SecretService.class);
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

    public List<SecretVO> getSecretInfo(int start, int size, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<SecretContentEntity> list = secretMapper.selectSecret(start, size);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        List<SecretVO> result = new ArrayList<>();
        for (SecretContentEntity entity : list) {
            SecretVO vo = secretConverter.toVO(entity);
            vo.setCommentCount(secretMapper.selectSecretCommentCount(entity.getId()));
            vo.setLikeCount(secretMapper.selectSecretLikeCount(entity.getId()));
            vo.setLiked(secretMapper.selectSecretLike(entity.getId(), user.getUsername()));
            result.add(vo);
        }
        return result;
    }

    public List<SecretVO> getSecretInfo(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<SecretContentEntity> list = secretMapper.selectSecretByUsername(user.getUsername());
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return secretConverter.toVOList(list);
    }

    public List<SecretCommentVO> getSecretComments(int contentId) throws Exception {
        List<SecretCommentEntity> list = secretMapper.selectSecretCommentsByContentId(contentId);
        if (list == null) return new ArrayList<>();
        return secretCommentConverter.toVOList(list);
    }

    public boolean checkSecretInfoExist(int id) throws Exception {
        SecretContentEntity entity = secretMapper.selectSecretByID(id);
        return entity != null;
    }

    public String getSecretVoiceURL(int id) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "secret/voice/" + id + ".mp3"
                , 30, TimeUnit.MINUTES);
    }

    public void uploadVoiceSecret(int id, InputStream inputStream) {
        r2StorageService.uploadObject("gdeiassistant-userdata", "secret/voice/" + id + ".mp3", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            logger.error("关闭树洞语音上传流失败，id={}", id, e);
        }
    }

    public SecretVO getSecretDetailInfo(int id, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        SecretContentEntity entity = secretMapper.selectSecretByID(id);
        if (entity == null) {
            throw new DataNotExistException("查询的树洞消息不存在");
        }
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

    public void addSecretComment(int id, String sessionId, String comment) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        SecretCommentEntity entity = new SecretCommentEntity();
        entity.setContentId(id);
        entity.setUsername(user.getUsername());
        entity.setComment(comment);
        entity.setAvatarTheme((int) (Math.random() * 50));
        secretMapper.insertSecretComment(entity);
    }

    public void changeUserLikeState(boolean like, int id, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (like) {
            secretMapper.insertSecretLike(id, user.getUsername());
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

    private SecretContentEntity dtoToEntity(SecretPublishDTO dto, String username) {
        SecretContentEntity e = new SecretContentEntity();
        e.setUsername(username);
        e.setTheme(dto.getTheme());
        e.setContent(dto.getContent());
        e.setType(dto.getType());
        e.setTimer(dto.getTimer());
        return e;
    }
}
