package cn.gdeiassistant.Service.UserLogin;

import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User.UserMapper;
import cn.gdeiassistant.Service.UserData.UserDataService;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class UserLoginService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private cn.gdeiassistant.Service.UserLogin.UserCertificateService userCertificateService;

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
    public void UserLogin(String sessionId, String username, String password) throws Exception {
        //查询数据库，若账号密码相同，则通过登录校验
        if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
            User queryUser = userMapper.selectUser(username);
            if (queryUser != null) {
                //将数据库查询的用户数据与用户提交的用户信息进行对比
                if (queryUser.equals(new User(username, password))) {
                    //登录成功
                    userCertificateService.SaveUserLoginCertificate(sessionId, username, password);
                    return;
                }
            }
        }
        //密码不匹配或用户账号未在数据库中，进行一次教务系统校验
        userCertificateService.SyncUpdateSessionCertificate(sessionId
                , username, password);
        //校验通过，将用户数据同步到数据库

        userDataService.SyncUserData(new User(username, password));
    }


}
