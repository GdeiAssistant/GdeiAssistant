package cn.gdeiassistant.core.photograph.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.photograph.converter.PhotographCommentConverter;
import cn.gdeiassistant.core.photograph.converter.PhotographConverter;
import cn.gdeiassistant.core.photograph.mapper.PhotographMapper;
import cn.gdeiassistant.core.photograph.pojo.dto.PhotographPublishDTO;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographCommentVO;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PhotographService {

    private static final Logger logger = LoggerFactory.getLogger(PhotographService.class);

    @Autowired
    private PhotographMapper photographMapper;

    @Autowired
    private PhotographConverter photographConverter;

    @Autowired
    private PhotographCommentConverter photographCommentConverter;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    public int queryPhotoStatisticalData() {
        Integer n = photographMapper.selectPhotographImageCount();
        return n != null ? n : 0;
    }

    public int queryCommentStatisticalData() {
        Integer n = photographMapper.selectPhotographCommentCount();
        return n != null ? n : 0;
    }

    public int queryLikeStatisticalData() {
        Integer n = photographMapper.selectPhotographLikeCount();
        return n != null ? n : 0;
    }

    public List<PhotographVO> queryPhotographList(int start, int size, int type, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<PhotographEntity> list = photographMapper.selectPhotograph(start, size, type, user.getUsername());
        if (list == null) return new ArrayList<>();
        List<PhotographVO> result = new ArrayList<>();
        for (PhotographEntity e : list) {
            if (e.getId() == null) continue;
            PhotographVO vo = photographConverter.toVO(e);
            vo.setFirstImageUrl(getPhotographItemPictureURL(e.getId(), 1));
            result.add(vo);
        }
        return result;
    }

    public List<PhotographVO> queryMyPhotographList(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<PhotographEntity> list = photographMapper.selectPhotographByUsername(start, size, user.getUsername());
        if (list == null) return new ArrayList<>();
        List<PhotographVO> result = new ArrayList<>();
        for (PhotographEntity e : list) {
            if (e.getId() == null) continue;
            PhotographVO vo = photographConverter.toVO(e);
            if (e.getCount() != null && e.getCount() >= 1) {
                vo.setFirstImageUrl(getPhotographItemPictureURL(e.getId(), 1));
            }
            result.add(vo);
        }
        return result;
    }

    public PhotographVO getPhotographById(int id, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<PhotographEntity> list = photographMapper.selectPhotographByIdAndUsername(id, user.getUsername());
        if (list == null || list.isEmpty()) {
            throw new DataNotExistException("照片信息不存在");
        }
        PhotographEntity e = list.get(0);
        PhotographVO vo = photographConverter.toVO(e);
        List<String> urls = new ArrayList<>();
        int count = e.getCount() != null ? e.getCount() : 0;
        for (int i = 1; i <= count; i++) {
            urls.add(getPhotographItemPictureURL(e.getId(), i));
        }
        vo.setImageUrls(urls);
        if (e.getPhotographCommentList() != null) {
            vo.setPhotographCommentList(photographCommentConverter.toVOList(e.getPhotographCommentList()));
        }
        return vo;
    }

    public List<PhotographCommentVO> queryPhotographCommentList(int id) {
        List<PhotographCommentEntity> list = photographMapper.selectPhotographCommentByPhotoId(id);
        if (list == null) return new ArrayList<>();
        return photographCommentConverter.toVOList(list);
    }

    public int addPhotograph(PhotographPublishDTO dto, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        PhotographEntity entity = dtoToEntity(dto, user.getUsername());
        photographMapper.insertPhotograph(entity);
        return entity.getId();
    }

    @Transactional("appTransactionManager")
    public void addPhotographComment(int id, String comment, String sessionId) throws DataNotExistException {
        if (comment == null || comment.trim().isEmpty() || comment.length() > 50) {
            throw new IllegalArgumentException("评论内容不能为空且不能超过 50 字");
        }
        Integer photographCount = photographMapper.selectPhotographCountById(id);
        if (photographCount == null || photographCount == 0) {
            throw new DataNotExistException("照片信息不存在");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        PhotographCommentEntity entity = new PhotographCommentEntity();
        entity.setPhotoId(id);
        entity.setComment(comment);
        entity.setUsername(user.getUsername());
        photographMapper.insertPhotographComment(entity);
        PhotographEntity photograph = getNotificationTarget(id, user.getUsername());
        interactionNotificationService.createInteractionNotification(
                "photograph",
                "comment",
                photograph != null ? photograph.getUsername() : null,
                user.getUsername(),
                String.valueOf(id),
                entity.getCommentId() == null ? null : String.valueOf(entity.getCommentId()),
                "comment",
                "作品收到新评论",
                user.getUsername() + " 评论了你的作品：" + comment
        );
    }

    public void uploadPhotographItemPicture(int id, int index, InputStream inputStream) {
        try {
            r2StorageService.uploadObject("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传拍好校园图片失败，id={}, index={}", id, index, e);
            throw new RuntimeException("拍好校园图片上传失败", e);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }

    public void movePhotographItemPictureFromTempObject(int id, int index, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "photograph/" + id + "_" + index + ".jpg");
    }

    public void deletePhotograph(int id) {
        photographMapper.deletePhotograph(id);
    }

    public void deletePhotographImages(int id, int count) {
        for (int i = 1; i <= count; i++) {
            try {
                r2StorageService.deleteObject("gdeiassistant-userdata", "photograph/" + id + "_" + i + ".jpg");
            } catch (Exception ignored) {}
        }
    }

    public String getPhotographItemPictureURL(int id, int index) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "photograph/" + id + "_" + index + ".jpg", 30, TimeUnit.MINUTES);
    }

    @Transactional("appTransactionManager")
    public void LikePhotograph(int id, String sessionId) throws DataNotExistException {
        Integer photographCount = photographMapper.selectPhotographCountById(id);
        if (photographCount == null || photographCount == 0) {
            throw new DataNotExistException("照片信息不存在");
        }
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer count = photographMapper.selectPhotographLikeCountByPhotoIdAndUsername(id, user.getUsername());
        if (count == null || count == 0) {
            photographMapper.insertPhotographLike(id, user.getUsername());
            PhotographEntity photograph = getNotificationTarget(id, user.getUsername());
            interactionNotificationService.createInteractionNotification(
                    "photograph",
                    "like",
                    photograph != null ? photograph.getUsername() : null,
                    user.getUsername(),
                    String.valueOf(id),
                    null,
                    "like",
                    "作品收到新点赞",
                    user.getUsername() + " 点赞了你的作品"
            );
        }
    }

    private PhotographEntity dtoToEntity(PhotographPublishDTO dto, String username) {
        PhotographEntity e = new PhotographEntity();
        e.setTitle(dto.getTitle());
        e.setContent(dto.getContent());
        e.setCount(dto.getCount());
        e.setType(dto.getType());
        e.setUsername(username);
        return e;
    }

    private PhotographEntity getNotificationTarget(int id, String username) {
        List<PhotographEntity> list = photographMapper.selectPhotographByIdAndUsername(id, username);
        return list == null || list.isEmpty() ? null : list.get(0);
    }
}
