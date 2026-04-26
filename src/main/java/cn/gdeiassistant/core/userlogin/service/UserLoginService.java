package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.userData.service.UserDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserLoginService {

    @Autowired
    private cn.gdeiassistant.core.userLogin.service.UserCertificateService userCertificateService;

    @Autowired
    private UserDataService userDataService;

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
        completeRemoteSchoolLogin(sessionId, username, password, persistCredential);
    }

    private void completeRemoteSchoolLogin(String sessionId, String username, String password, boolean persistCredential) throws Exception {
        userCertificateService.syncUpdateSessionCertificate(sessionId, username, password);
        userDataService.syncUserData(new User(username, password), persistCredential);
        userCertificateService.saveUserLoginCertificate(sessionId, username, password);
    }

}
