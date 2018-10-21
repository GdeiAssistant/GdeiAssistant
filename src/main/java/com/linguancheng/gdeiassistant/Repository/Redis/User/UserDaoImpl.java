package com.linguancheng.gdeiassistant.Repository.Redis.User;

import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/10/21
 */

@Repository
public class UserDaoImpl implements UserDao {

    @Autowired
    private RedisTemplate<String, Map<String, String>> redisTemplate;

    @Override
    public UserCertificate queryUserCertificate(String username) {
        Map<String, String> map = redisTemplate.opsForValue().get(username);
        if (map != null) {
            UserCertificate userCertificate = new UserCertificate();
            User user = new User();
            user.setUsername(username);
            user.setPassword(map.get("password"));
            user.setKeycode(map.get("keycode"));
            user.setNumber(map.get("number"));
            userCertificate.setUser(user);
            userCertificate.setTimestamp(Long.valueOf(map.get("timestamp")));
            return userCertificate;
        }
        return null;
    }

    @Override
    public void saveUserCertificate(UserCertificate userCertificate) {
        Map<String, String> map = new HashMap<>();
        User user = userCertificate.getUser();
        map.put("password", user.getPassword());
        map.put("keycode", user.getKeycode());
        map.put("number", user.getNumber());
        map.put("timestamp", String.valueOf(userCertificate.getTimestamp()));
        redisTemplate.opsForValue().set(user.getUsername(), map);
        redisTemplate.expire(user.getUsername(), 1, TimeUnit.HOURS);
    }
}
