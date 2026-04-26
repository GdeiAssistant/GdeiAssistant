package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.userData.service.UserDataService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class UserLoginService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private cn.gdeiassistant.core.userLogin.service.UserCertificateService userCertificateService;

    @Autowired
    private UserDataService userDataService;

    @Autowired
    private CampusCredentialService campusCredentialService;

    /**
     * 用户登录验证
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     */
    public void userLogin(String sessionId, String username, String password) throws Exception {
        userLogin(sessionId, username, password, true);
    }

    /**
     * 用户登录验证
     *
     * @param sessionId
     * @param username
     * @param password
     * @param persistCredential 是否允许更新长期保存的校园凭证
     */
    public void userLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        //查询数据库，若账号密码相同，则通过登录校验
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            UserEntity queryUser = userMapper.selectUser(username);
            if (queryUser != null
                    && username.equals(queryUser.getUsername())
                    && password.equals(queryUser.getPassword())
                    && campusCredentialService.isEffectiveQuickAuthEnabled(username)) {
                userCertificateService.saveUserLoginCertificate(sessionId, username, password);
                return;
            }
        }
        userCertificateService.syncUpdateSessionCertificate(sessionId, username, password);
        userDataService.syncUserData(new User(username, password), persistCredential);
    }


}
