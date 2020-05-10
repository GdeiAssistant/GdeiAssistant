package edu.gdei.gdeiassistant.Service.Topic;

import com.aliyun.oss.OSSClient;
import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.Topic;
import edu.gdei.gdeiassistant.Pojo.Entity.TopicLike;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Topic.TopicMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class TopicService {

    private String accessKeyID;

    private String accessKeySecret;

    private String endpoint;

    @Autowired
    private TopicMapper topicMapper;

    @Value("#{propertiesReader['oss.accessKeySecret']}")
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Value("#{propertiesReader['oss.accessKeyID']}")
    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    @Value("#{propertiesReader['oss.endpoint']}")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 加载话题信息
     *
     * @param start
     * @param size
     * @param username
     * @return
     * @throws WsgException
     */
    public List<Topic> QueryTopic(int start, int size, String username) throws WsgException {
        List<Topic> topicList = topicMapper.selectTopicPage(start, size, StringEncryptUtils.encryptString(username));
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
     * @param start
     * @param size
     * @param username
     * @param keyword
     * @return
     * @throws WsgException
     */
    public List<Topic> QueryTopicByKeyword(int start, int size, String username, String keyword) throws WsgException {
        List<Topic> topicList = topicMapper.selectTopicPageByKeyword(start, size, StringEncryptUtils.encryptString(username), keyword);
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
     * @param username
     * @throws WsgException
     * @throws DataNotExistException
     */
    public void LikeTopic(int id, String username) throws WsgException, DataNotExistException {
        Topic topic = topicMapper.selectTopicById(id, StringEncryptUtils.encryptString(username));
        if (topic != null) {
            TopicLike topicLike = topicMapper.selectTopicLike(id, StringEncryptUtils.encryptString(username));
            if (topicLike == null) {
                topicMapper.insertTopicLike(id, StringEncryptUtils.encryptString(username));
            }
            return;
        }
        throw new DataNotExistException("该话题信息不存在");
    }

    /**
     * 添加话题信息
     *
     * @param topic
     * @param username
     */
    public Topic AddTopic(Topic topic, String username) throws WsgException {
        Topic data = new Topic();
        data.setUsername(StringEncryptUtils.encryptString(username));
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
        String url = null;
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //检查用户数据是否存在
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg")) {
            //设置过期时间10分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 90);
            // 生成URL
            url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg"
                    , expiration).toString().replace("http", "https");
        }
        ossClient.shutdown();
        return url;
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
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "topic/" + id + "_" + index + ".jpg", inputStream);
        ossClient.shutdown();
    }
}
