package cn.gdeiassistant.Service.Secret;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.Secret;
import cn.gdeiassistant.Pojo.Entity.SecretComment;
import cn.gdeiassistant.Pojo.Entity.SecretContent;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Secret.SecretMapper;
import cn.gdeiassistant.Tools.SpringUtils.OSSUtils;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Resource(name = "secretMapper")
    private SecretMapper secretMapper;

    @Autowired
    private OSSUtils ossUtils;

    /**
     * 获取树洞消息
     *
     * @param start
     * @param size
     * @return
     */
    public List<Secret> GetSecretInfo(int start, int size, String username) throws Exception {
        List<Secret> secretList = secretMapper.selectSecret(start, size);
        if (secretList == null || secretList.isEmpty()) {
            return new ArrayList<>();
        }
        for (Secret secret : secretList) {
            //加载点赞数量/评论数量/点赞状态
            secret.setCommentCount(secretMapper.selectSecretCommentCount(secret.getId()));
            secret.setLikeCount(secretMapper.selectSecretLikeCount(secret.getId()));
            secret.setLiked(secretMapper.selectSecretLike(secret.getId(), StringEncryptUtils.encryptString(username)));
        }
        return secretList;
    }

    /**
     * 获取用户发布的树洞信息
     *
     * @param username
     * @return
     */
    public List<Secret> GetSecretInfo(String username) throws Exception {
        List<Secret> secretList = secretMapper.selectSecretByUsername(StringEncryptUtils.encryptString(username));
        if (secretList == null || secretList.isEmpty()) {
            return new ArrayList<>();
        }
        return secretList;
    }

    /**
     * 检查树洞信息是否存在
     *
     * @param id
     * @return
     */
    public boolean CheckSecretInfoExist(int id) throws Exception {
        Secret secret = secretMapper.selectSecretByID(id);
        return secret != null;
    }

    /**
     * 获取校园树洞语音音频文件地址
     *
     * @param id
     * @return
     */
    public String GetSecretVoiceURL(int id) {
        return ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "secret/voice/" + id + ".mp3"
                ,30, TimeUnit.MINUTES);
    }

    /**
     * 上传语音树洞录音对象
     *
     * @param id
     * @param inputStream
     */
    public void UploadVoiceSecret(int id, InputStream inputStream) {
        ossUtils.UploadOSSObject("gdeiassistant-userdata", "secret/voice/" + id + ".mp3", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取树洞消息详细信息
     *
     * @param id
     * @return
     */
    public Secret GetSecretDetailInfo(int id, String username) throws Exception {
        Secret secret = secretMapper.selectSecretByID(id);
        if (secret != null) {
            if (secret.getType() == 1) {
                //获取语音音频文件地址
                secret.setVoiceURL(GetSecretVoiceURL(secret.getId()));
            }
            //加载点赞数量/评论数量/点赞状态
            secret.setCommentCount(secretMapper.selectSecretCommentCount(secret.getId()));
            secret.setLikeCount(secretMapper.selectSecretLikeCount(secret.getId()));
            secret.setLiked(secretMapper.selectSecretLike(secret.getId(), StringEncryptUtils.encryptString(username)));
            return secret;
        }
        throw new DataNotExistException("查询的树洞消息不存在");
    }

    /**
     * 添加树洞信息
     *
     * @param username
     * @param secret
     * @return
     */
    public Integer AddSecretInfo(String username, Secret secret) throws Exception {
        SecretContent secretContent = new SecretContent(secret, StringEncryptUtils.encryptString(username));
        secretMapper.insertSecret(secretContent);
        return secretContent.getId();
    }

    /**
     * 添加树洞信息评论
     *
     * @param id
     * @param username
     * @param comment
     * @return
     */
    public void AddSecretComment(int id, String username, String comment) throws Exception {
        SecretComment secretComment = new SecretComment();
        secretComment.setContentId(id);
        secretComment.setUsername(StringEncryptUtils.encryptString(username));
        secretComment.setComment(comment);
        secretComment.setAvatarTheme((int) (Math.random() * 50));
        secretMapper.insertSecretComment(secretComment);
    }

    /**
     * 更改用户点赞状态
     *
     * @param like
     * @param id
     * @param username
     * @return
     */
    public void ChangeUserLikeState(boolean like, int id, String username) throws Exception {
        if (like) {
            //点赞
            secretMapper.insertSecretLike(id, StringEncryptUtils.encryptString(username));
        } else {
            //取消点赞
            secretMapper.deleteSecretLike(id, StringEncryptUtils.encryptString(username));
        }
    }

    /**
     * 定时删除设置了定时删除的树洞消息
     *
     * @throws Exception
     */
    @Scheduled(fixedDelay = 300000)
    @Transactional("appTransactionManager")
    public void DeleteTimerSecretInfos() throws Exception {
        List<Secret> secretList = secretMapper.selectNotRemovedSecrets();
        for (Secret secret : secretList) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime time = LocalDateTime.ofInstant(secret.getPublishTime().toInstant()
                    , ZoneId.systemDefault());
            if (ChronoUnit.HOURS.between(time, now) >= 24) {
                secretMapper.deleteSecret(secret.getId());
            }
        }
    }
}
