package com.linguancheng.gdeiassistant.Service.Secret;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Secret.SecretMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.Secret;
import com.linguancheng.gdeiassistant.Pojo.Entity.SecretComment;
import com.linguancheng.gdeiassistant.Pojo.Entity.SecretContent;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class SecretService {

    @Resource(name = "secretMapper")
    private SecretMapper secretMapper;

    private Log log = LogFactory.getLog(SecretService.class);

    /**
     * 获取树洞消息
     *
     * @param start
     * @param size
     * @return
     */
    public BaseResult<List<Secret>, DataBaseResultEnum> GetSecretInfo(int start, int size, String username) {
        BaseResult<List<Secret>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<Secret> secretList = secretMapper.selectSecret(start, size);
            if (secretList == null || secretList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (Secret secret : secretList) {
                    //加载点赞数量/评论数量/点赞状态
                    secret.setCommentCount(secretMapper.selectSecretCommentCount(secret.getId()));
                    secret.setLikeCount(secretMapper.selectSecretLikeCount(secret.getId()));
                    secret.setLiked(secretMapper.selectSecretLike(secret.getId(), StringEncryptUtils.encryptString(username)));
                }
                result.setResultData(secretList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("获取树洞信息异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 获取用户发布的树洞信息
     *
     * @param username
     * @return
     */
    public BaseResult<List<Secret>, DataBaseResultEnum> GetSecretInfo(String username) {
        BaseResult<List<Secret>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<Secret> secretList = secretMapper.selectSecretByUsername(StringEncryptUtils.encryptString(username));
            if (secretList == null || secretList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                result.setResultData(secretList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("获取用户发布树洞信息异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 检查树洞信息是否存在
     *
     * @param id
     * @return
     */
    public boolean CheckSecretInfoExist(int id) {
        try {
            Secret secret = secretMapper.selectSecretByID(id);
            return secret != null;
        } catch (Exception e) {
            log.error("ID查询树洞信息异常：" + e);
            return false;
        }
    }

    /**
     * 获取树洞消息详细信息
     *
     * @param id
     * @return
     */
    public BaseResult<Secret, DataBaseResultEnum> GetSecretDetailInfo(int id, String username) {
        BaseResult<Secret, DataBaseResultEnum> result = new BaseResult<>();
        try {
            Secret secret = secretMapper.selectSecretByID(id);
            if (secret != null) {
                //加载关联的评论数据
                secret.getSecretCommentList();
                //加载点赞数量/评论数量/点赞状态
                secret.setCommentCount(secretMapper.selectSecretCommentCount(secret.getId()));
                secret.setLikeCount(secretMapper.selectSecretLikeCount(secret.getId()));
                secret.setLiked(secretMapper.selectSecretLike(secret.getId(), StringEncryptUtils.encryptString(username)));
                result.setResultData(secret);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            } else {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            }
        } catch (Exception e) {
            log.error("获取树洞详细信息异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 添加树洞信息
     *
     * @param username
     * @param secret
     * @return
     */
    public boolean AddSecretInfo(String username, Secret secret) {
        try {
            SecretContent secretContent = new SecretContent(secret, StringEncryptUtils.encryptString(username));
            secretMapper.insertSecret(secretContent);
            return true;
        } catch (Exception e) {
            log.error("添加树洞信息异常：" + e);
            return false;
        }
    }

    /**
     * 添加树洞信息评论
     *
     * @param id
     * @param username
     * @param comment
     * @return
     */
    public boolean AddSecretComment(int id, String username, String comment) {
        try {
            SecretComment secretComment = new SecretComment();
            secretComment.setId(id);
            secretComment.setUsername(StringEncryptUtils.encryptString(username));
            secretComment.setComment(comment);
            secretComment.setAvatarTheme((int) (Math.random() * 50));
            secretComment.setPublishTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            secretMapper.insertSecretComment(secretComment);
            return true;
        } catch (Exception e) {
            log.error("添加树洞评论异常：" + e);
            return false;
        }
    }

    /**
     * 更改用户点赞状态
     *
     * @param like
     * @param id
     * @param username
     * @return
     */
    public boolean ChangeUserLikeState(boolean like, int id, String username) {
        try {
            if (like) {
                //点赞
                secretMapper.insertSecretLike(id, StringEncryptUtils.encryptString(username));
            } else {
                //取消点赞
                secretMapper.deleteSecretLike(id, StringEncryptUtils.encryptString(username));
            }
            return true;
        } catch (Exception e) {
            log.error("更改树洞点赞状态异常：" + e);
            return false;
        }
    }

}
