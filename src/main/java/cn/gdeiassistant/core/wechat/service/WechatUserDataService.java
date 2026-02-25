package cn.gdeiassistant.core.wechat.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.wechatUser.mapper.WechatUserMapper;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class WechatUserDataService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "wechatUserMapper")
    private WechatUserMapper wechatUserMapper;

    /**
     * 查询微信账号绑定校园网络账号用户名
     *
     * @param wechatID
     * @return
     */
    public String queryWechatAttachUsername(String wechatID) throws Exception {
        String username = wechatUserMapper.selectUsername(wechatID);
        if (StringUtils.isNotBlank(username)) {
            return username;
        }
        return null;
    }

    /**
     * 通过WechatID对应的Username查询用户信息数据
     *
     * @param username
     * @return
     */
    public User queryWechatUserData(String username) throws Exception {
        UserEntity entity = userMapper.selectUser(username);
        if (entity == null) return null;
        User user = new User();
        user.setUsername(entity.getUsername());
        user.setPassword(entity.getPassword());
        return user;
    }

    /**
     * 同步微信账号与校园网络账号绑定的数据
     *
     * @param username
     * @param wechatUserID
     * @return
     */
    public void syncWechatUserData(String username, String wechatUserID) throws Exception {
        if (wechatUserMapper.selectUsername(wechatUserID) != null) {
            //更新微信账号绑定的校园网络账号
            wechatUserMapper.updateWechatUser(wechatUserID, username);
        } else {
            wechatUserMapper.insertWechatUser(wechatUserID, username);
        }
    }
}
