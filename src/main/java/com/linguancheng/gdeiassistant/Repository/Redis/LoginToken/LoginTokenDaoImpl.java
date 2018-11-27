package com.linguancheng.gdeiassistant.Repository.Redis.LoginToken;

import com.linguancheng.gdeiassistant.Pojo.Entity.AccessToken;
import com.linguancheng.gdeiassistant.Pojo.Entity.RefreshToken;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/28
 */

@Repository
public class LoginTokenDaoImpl implements LoginTokenDao {

    private Log log = LogFactory.getLog(LoginTokenDao.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 查找令牌签名对应的记录信息
     *
     * @param signature
     * @return
     */
    @Override
    public String QueryToken(String signature) {
        try {
            String value = redisTemplate.opsForValue().get(StringEncryptUtils
                    .SHA256MapString(signature));
            if (value != null) {
                return value;
            }
        } catch (Exception e) {
            log.error("查找权限令牌记录异常：", e);
        }
        return null;
    }

    /**
     * 插入权限令牌信息
     *
     * @param token
     * @return
     */
    @Override
    public Boolean InsertAccessToken(AccessToken token) {
        try {
            //保存权限令牌，设置有效期为7天
            redisTemplate.opsForValue().set(StringEncryptUtils
                    .SHA256MapString(token.getSignature()), token.getIp());
            redisTemplate.expire(StringEncryptUtils
                    .SHA256MapString(token.getSignature()), 7, TimeUnit.DAYS);
            return true;
        } catch (Exception e) {
            log.error("保存权限令牌信息失败", e);
            return false;
        }
    }

    /**
     * 插入刷新令牌信息
     *
     * @param token
     * @return
     */
    @Override
    public Boolean InsertRefreshToken(RefreshToken token) {
        try {
            //保存刷新令牌，设置有效期为30天
            redisTemplate.opsForValue().set(StringEncryptUtils.SHA256MapString(token.getSignature())
                    , token.getTokenSignature());
            redisTemplate.expire(StringEncryptUtils.SHA256MapString(token.getSignature())
                    , 30, TimeUnit.DAYS);
            return true;
        } catch (Exception e) {
            log.error("保存刷新令牌信息失败", e);
            return false;
        }
    }

    @Override
    public Boolean UpdateAccessToken(AccessToken token) {
        try {
            //更新权限令牌信息，但不重新设置有效期
            redisTemplate.opsForValue().set(StringEncryptUtils.SHA256MapString(token.getSignature()), token.getIp());
            return true;
        } catch (Exception e) {
            log.error("保存刷新令牌信息失败", e);
            return false;
        }
    }

    /**
     * 删除令牌
     *
     * @param signature
     * @return
     */
    @Override
    public Boolean DeleteToken(String signature) {
        try {
            return redisTemplate.expire(StringEncryptUtils.SHA256MapString(signature)
                    , 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("删除令牌信息失败", e);
            return false;
        }
    }

}
