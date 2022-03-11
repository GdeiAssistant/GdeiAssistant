package cn.gdeiassistant.Service.Topic;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.Topic;
import cn.gdeiassistant.Pojo.Entity.TopicLike;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Topic.TopicMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.OSSUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class TopicService {

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private TopicMapper topicMapper;

    @Autowired
    private OSSUtils ossUtils;

    /**
     * 加载话题信息
     *
     * @param sessionId
     * @param start
     * @param size
     * @return
     * @throws WsgException
     */
    public List<Topic> QueryTopic(String sessionId, int start, int size) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);

        List<Topic> topicList = topicMapper.selectTopicPage(start, size
                , StringEncryptUtils.encryptString(user.getUsername()));
        if (topicList != null && !topicList.isEmpty()) {
            for (Topic topic : topicList) {
                topic.setUsername(StringEncryptUtils.decryptString(topic.getUsername()));
            }
            return topicList;
        }
        return new ArrayList<>();
    }

    /**
     * 关键词加载话题信息
     *
     * @param sessionId
     * @param start
     * @param size
     * @param keyword
     * @return
     * @throws WsgException
     */
    public List<Topic> QueryTopicByKeyword(String sessionId, int start, int size, String keyword) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<Topic> topicList = topicMapper.selectTopicPageByKeyword(start, size
                , StringEncryptUtils.encryptString(user.getUsername()), keyword);
        if (topicList != null && !topicList.isEmpty()) {
            for (Topic topic : topicList) {
                topic.setUsername(StringEncryptUtils.decryptString(topic.getUsername()));
            }
            return topicList;
        }
        return new ArrayList<>();
    }

    /**
     * 点赞话题信息
     *
     * @param id
     * @param sessionId
     * @throws WsgException
     * @throws DataNotExistException
     */
    public void LikeTopic(int id, String sessionId) throws WsgException, DataNotExistException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Topic topic = topicMapper.selectTopicById(id, StringEncryptUtils.encryptString(user.getUsername()));
        if (topic != null) {
            TopicLike topicLike = topicMapper.selectTopicLike(id, StringEncryptUtils.encryptString(user.getUsername()));
            if (topicLike == null) {
                topicMapper.insertTopicLike(id, StringEncryptUtils.encryptString(user.getUsername()));
            }
            return;
        }
        throw new DataNotExistException("该话题信息不存在");
    }

    /**
     * 添加话题信息
     *
     * @param topic
     * @param sessionId
     */
    public Topic AddTopic(Topic topic, String sessionId) throws WsgException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Topic data = new Topic();
        data.setUsername(StringEncryptUtils.encryptString(user.getUsername()));
        data.setTopic(topic.getTopic());
        data.setContent(topic.getContent());
        data.setCount(topic.getCount());
        topicMapper.insertTopic(data);
        return data;
    }

    /**
     * 下载话题信息图片
     *
     * @param id
     * @param index
     * @return
     */
    public String DownloadTopicItemPicture(int id, int index) {
        return ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg"
                , 90, TimeUnit.MINUTES);
    }

    /**
     * 上传话题信息图片
     *
     * @param id
     * @param index
     * @param inputStream
     */
    @Async
    public void UploadTopicItemPicture(int id, int index, InputStream inputStream) {
        ossUtils.UploadOSSObject("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg"
                , inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
