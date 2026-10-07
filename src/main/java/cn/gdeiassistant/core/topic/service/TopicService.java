package cn.gdeiassistant.core.topic.service;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.topic.converter.TopicConverter;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.pojo.entity.TopicEntity;
import cn.gdeiassistant.core.topic.pojo.entity.TopicLikeEntity;
import cn.gdeiassistant.core.topic.pojo.vo.TopicVO;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.CampusAccountView;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.common.tools.utils.AnonymizeUtils;
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
public class TopicService {

    private static final Logger logger = LoggerFactory.getLogger(TopicService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private TopicConverter topicConverter;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private cn.gdeiassistant.core.objectstorage.service.UploadService uploads;

    @Autowired
    private cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    @Autowired(required = false)
    private UserMapper userMapper;

    @Autowired(required = false)
    private ProfileMapper profileMapper;

    @Autowired
    private cn.gdeiassistant.core.user.service.PublicAuthorResolver publicAuthors;

    public List<TopicVO> queryTopic(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicPage(start, size, user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        var authors = publicAuthors.resolveAll(list.stream().map(TopicEntity::getUsername).toList());
        List<TopicVO> voList = new ArrayList<>();
        for (TopicEntity e : list) {
            if (e.getCount() != null && e.getCount() >= 1) {
                e.setFirstImageUrl(downloadTopicItemPicture(e.getId(), 1));
            }
            voList.add(toPublicTopicVO(e, authors.get(e.getUsername())));
        }
        return voList;
    }

    public List<TopicVO> queryTopicByKeyword(String sessionId, int start, int size, String keyword) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicPageByKeyword(start, size, user.getUsername(), keyword);
        if (list == null || list.isEmpty()) return new ArrayList<>();
        var authors = publicAuthors.resolveAll(list.stream().map(TopicEntity::getUsername).toList());
        List<TopicVO> voList = new ArrayList<>();
        for (TopicEntity e : list) {
            if (e.getCount() != null && e.getCount() >= 1) {
                e.setFirstImageUrl(downloadTopicItemPicture(e.getId(), 1));
            }
            voList.add(toPublicTopicVO(e, authors.get(e.getUsername())));
        }
        return voList;
    }

    public List<TopicVO> queryMyTopicList(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicByUsername(start, size, user.getUsername(), user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        var authors = publicAuthors.resolveAll(list.stream().map(TopicEntity::getUsername).toList());
        List<TopicVO> voList = new ArrayList<>();
        for (TopicEntity e : list) {
            if (e.getCount() != null && e.getCount() >= 1) {
                e.setFirstImageUrl(downloadTopicItemPicture(e.getId(), 1));
            }
            voList.add(toPublicTopicVO(e, authors.get(e.getUsername())));
        }
        return voList;
    }

    public TopicVO queryTopicById(int id, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        TopicEntity entity = topicMapper.selectTopicById(id, user.getUsername());
        if (entity == null) throw new DataNotExistException("该话题信息不存在");
        if (entity.getCount() != null && entity.getCount() > 0) {
            List<String> urls = new ArrayList<>();
            for (int i = 1; i <= entity.getCount(); i++) {
                urls.add(downloadTopicItemPicture(id, i));
            }
            entity.setImageUrls(urls);
        }
        return toPublicTopicVO(entity);
    }

    @Transactional("appTransactionManager")
    public void likeTopic(int id, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        TopicEntity entity = topicMapper.selectTopicById(id, user.getUsername());
        if (entity == null) throw new DataNotExistException("该话题信息不存在");
        TopicLikeEntity like = topicMapper.selectTopicLike(id, user.getUsername());
        if (like == null) {
            int inserted = topicMapper.insertTopicLike(id, user.getUsername());
            if (inserted > 0) {
                String displayName = resolveDisplayName(user.getUsername());
                interactionNotificationService.createInteractionNotification(
                        "topic",
                        "like",
                        entity.getUsername(),
                        user.getUsername(),
                        String.valueOf(id),
                        null,
                        "like",
                        "话题收到新点赞",
                        displayName + " 点赞了你的话题"
                );
            }
        }
    }

    public TopicVO addTopic(TopicPublishDTO dto, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        TopicEntity entity = new TopicEntity();
        entity.setUsername(user.getUsername());
        entity.setTopic(dto.getTopic());
        entity.setContent(dto.getContent());
        entity.setCount(dto.getCount());
        topicMapper.insertTopic(entity);
        return toPublicTopicVO(entity);
    }

    public String downloadTopicItemPicture(int id, int index) {
        return storedAssets.generatePresignedUrl(null, "topic/" + id + "_" + index + ".jpg", 90, TimeUnit.MINUTES);
    }

    public void uploadTopicItemPicture(int id, int index, InputStream inputStream) {
        try {
            storedAssets.uploadObject(null, "topic/" + id + "_" + index + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传话题图片失败，id={}，index={}", id, index, e);
            throw new RuntimeException("话题图片上传失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭话题图片上传输入流失败，id={}，index={}", id, index, e);
                }
            }
        }
    }

    public void moveTopicItemPictureFromTempObject(int id, int index, String objectKey, String sessionId) {
        uploads.moveUpload(sessionId, objectKey, "topic/" + id + "_" + index + ".jpg");
    }

    public void deleteTopic(int id) {
        topicMapper.deleteTopic(id);
    }

    private TopicVO toPublicTopicVO(TopicEntity entity) {
        return toPublicTopicVO(entity, publicAuthors.resolve(entity.getUsername()));
    }

    private TopicVO toPublicTopicVO(TopicEntity entity, cn.gdeiassistant.core.user.service.PublicAuthorResolver.AuthorPublic author) {
        TopicVO view = topicConverter.toVO(entity);
        view.setUsername(author == null ? "用户" : author.displayName());
        view.setAuthorId(author == null ? null : author.authorId());
        return view;
    }

    private String resolveDisplayName(String campusUsername) {
        if (profileMapper != null && campusUsername != null) {
            ProfileEntity profile = profileMapper.selectUserProfile(campusUsername);
            if (profile != null && profile.getNickname() != null && !profile.getNickname().isBlank()) {
                return profile.getNickname();
            }
        }
        return "用户";
    }

    public void deleteTopicImages(int id, int count) {
        for (int i = 1; i <= count; i++) {
            try {
                storedAssets.deleteObject(null, "topic/" + id + "_" + i + ".jpg");
            } catch (Exception e) {
                logger.warn("删除话题图片失败，id={}，index={}", id, i, e);
            }
        }
    }

    @org.springframework.transaction.annotation.Transactional(value="appTransactionManager", rollbackFor=Exception.class)
    public void publishTopic(TopicPublishDTO dto, String sessionId,
            org.springframework.web.multipart.MultipartFile[] images, String[] imageKeys) throws Exception {
        var created = addTopic(dto, sessionId);
        if (imageKeys != null && imageKeys.length > 0) {
            for (int i=0; i<imageKeys.length; i++) moveTopicItemPictureFromTempObject(created.getId(), i+1, imageKeys[i], sessionId);
        } else if (images != null) {
            int index = 1;
            for (var image : images) {
                if (image != null && !image.isEmpty()) uploadTopicItemPicture(created.getId(), index++, image.getInputStream());
            }
        }
    }

}
