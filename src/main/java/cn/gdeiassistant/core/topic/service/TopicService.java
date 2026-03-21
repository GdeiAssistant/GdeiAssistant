package cn.gdeiassistant.core.topic.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.topic.converter.TopicConverter;
import cn.gdeiassistant.core.topic.mapper.TopicMapper;
import cn.gdeiassistant.core.topic.pojo.dto.TopicPublishDTO;
import cn.gdeiassistant.core.topic.pojo.entity.TopicEntity;
import cn.gdeiassistant.core.topic.pojo.entity.TopicLikeEntity;
import cn.gdeiassistant.core.topic.pojo.vo.TopicVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
    private InteractionNotificationService interactionNotificationService;

    public List<TopicVO> queryTopic(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicPage(start, size, user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<TopicVO> voList = new ArrayList<>();
        for (TopicEntity e : list) {
            if (e.getCount() != null && e.getCount() >= 1) {
                e.setFirstImageUrl(downloadTopicItemPicture(e.getId(), 1));
            }
            voList.add(topicConverter.toVO(e));
        }
        return voList;
    }

    public List<TopicVO> queryTopicByKeyword(String sessionId, int start, int size, String keyword) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicPageByKeyword(start, size, user.getUsername(), keyword);
        return list == null || list.isEmpty() ? new ArrayList<>() : topicConverter.toVOList(list);
    }

    public List<TopicVO> queryMyTopicList(String sessionId, int start, int size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<TopicEntity> list = topicMapper.selectTopicByUsername(start, size, user.getUsername(), user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<TopicVO> voList = new ArrayList<>();
        for (TopicEntity e : list) {
            if (e.getCount() != null && e.getCount() >= 1) {
                e.setFirstImageUrl(downloadTopicItemPicture(e.getId(), 1));
            }
            voList.add(topicConverter.toVO(e));
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
        return topicConverter.toVO(entity);
    }

    @Transactional("appTransactionManager")
    public void likeTopic(int id, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        TopicEntity entity = topicMapper.selectTopicById(id, user.getUsername());
        if (entity == null) throw new DataNotExistException("该话题信息不存在");
        TopicLikeEntity like = topicMapper.selectTopicLike(id, user.getUsername());
        if (like == null) {
            topicMapper.insertTopicLike(id, user.getUsername());
            interactionNotificationService.createInteractionNotification(
                    "topic",
                    "like",
                    entity.getUsername(),
                    user.getUsername(),
                    String.valueOf(id),
                    null,
                    "like",
                    "话题收到新点赞",
                    user.getUsername() + " 点赞了你的话题"
            );
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
        return topicConverter.toVO(entity);
    }

    public String downloadTopicItemPicture(int id, int index) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg", 90, TimeUnit.MINUTES);
    }

    @Async
    public void uploadTopicItemPicture(int id, int index, InputStream inputStream) {
        try {
            r2StorageService.uploadObject("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传话题图片失败，id={}，index={}", id, index, e);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }

    public void moveTopicItemPictureFromTempObject(int id, int index, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "topic/" + id + "_" + index + ".jpg");
    }
}
