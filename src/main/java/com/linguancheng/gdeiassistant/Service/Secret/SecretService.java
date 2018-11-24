package com.linguancheng.gdeiassistant.Service.Secret;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Secret.SecretMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.Secret;
import com.linguancheng.gdeiassistant.Pojo.Entity.SecretComment;
import com.linguancheng.gdeiassistant.Pojo.Entity.SecretContent;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SecretService {

    @Resource(name = "secretMapper")
    private SecretMapper secretMapper;

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
     * 获取树洞消息详细信息
     *
     * @param id
     * @return
     */
    public Secret GetSecretDetailInfo(int id, String username) throws Exception {
        Secret secret = secretMapper.selectSecretByID(id);
        if (secret != null) {
            //加载关联的评论数据
            secret.getSecretCommentList();
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
    public void AddSecretInfo(String username, Secret secret) throws Exception {
        SecretContent secretContent = new SecretContent(secret, StringEncryptUtils.encryptString(username));
        secretMapper.insertSecret(secretContent);
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
        secretComment.setId(id);
        secretComment.setUsername(StringEncryptUtils.encryptString(username));
        secretComment.setComment(comment);
        secretComment.setAvatarTheme((int) (Math.random() * 50));
        secretComment.setPublishTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
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
}
