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
        if (canUseSavedCredentialShortcut(username, password)) {
            completeLocalQuickAuthLogin(sessionId, username, password);
            return;
        }
        completeRemoteSchoolLogin(sessionId, username, password, persistCredential);
    }

    private boolean canUseSavedCredentialShortcut(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            return false;
        }
        UserEntity queryUser = userMapper.selectUser(username);
        return queryUser != null
                && username.equals(queryUser.getUsername())
                && password.equals(queryUser.getPassword())
                && campusCredentialService.isEffectiveQuickAuthEnabled(username);
    }

    private void completeLocalQuickAuthLogin(String sessionId, String username, String password) {
        userCertificateService.saveUserLoginCertificate(sessionId, username, password);
    }

    private void completeRemoteSchoolLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        userCertificateService.syncUpdateSessionCertificate(sessionId, username, password);
        userDataService.syncUserData(new User(username, password), persistCredential);
    }

}
