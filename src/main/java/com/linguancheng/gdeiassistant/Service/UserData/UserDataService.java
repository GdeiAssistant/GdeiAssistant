package com.gdeiassistant.gdeiassistant.Service.UserData;

import com.gdeiassistant.gdeiassistant.Enum.Base.BoolResultEnum;
import com.gdeiassistant.gdeiassistant.Exception.CommonException.SyncTransactionException;
import com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Introduction;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Privacy;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.Profile;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.User;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Service.Profile.RealNameService;
import com.gdeiassistant.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
public class UserDataService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    private Log log = LogFactory.getLog(UserDataService.class);

    /**
     * 同步用户数据
     *
     * @param user
     * @return
     */
    @Transactional
    public void SyncUserData(User user) throws SyncTransactionException {
        try {
            User encryptUser = user.encryptUser();
            //检测数据库中有无该用户记录
            User queryUser = userMapper.selectUser(encryptUser.getUsername());
            if (queryUser != null) {
                //该用户已经存在,检查是否需要更新用户数据
                queryUser = queryUser.decryptUser();
                if (!queryUser.getUsername().equals(user.getUsername()) || !queryUser.getPassword().equals(user.getPassword())
                        || !queryUser.getKeycode().equals(user.getKeycode()) || !queryUser.getNumber().equals(user.getNumber())) {
                    userMapper.updateUser(encryptUser);
                }
            } else {
                //用户不存在,向数据库写入该用户数据
                userMapper.insertUser(encryptUser);
            }
            //个人资料初始化
            Profile profile = profileMapper.selectUserProfile(encryptUser.getUsername());
            if (profile == null) {
                profileMapper.initUserProfile(encryptUser.getUsername(), user.getUsername());
            }
            Privacy privacy = privacyMapper.selectPrivacy(encryptUser.getUsername());
            if (privacy == null) {
                privacyMapper.initPrivacy(encryptUser.getUsername());
            }
        } catch (Exception e) {
            log.error("同步用户数据异常：" + e);
            //抛出异常进行事务回滚
            throw new SyncTransactionException("同步用户数据异常，进行回滚");
        }
    }
}
