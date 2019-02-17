package edu.gdei.gdeiassistant.Service.UserData;

import edu.gdei.gdeiassistant.Pojo.Entity.Introduction;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class UserDataService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    /**
     * 同步用户数据
     *
     * @param user
     * @return
     */
    @Transactional
    public void SyncUserData(User user) throws Exception {
        User encryptUser = user.encryptUser();
        //检测数据库中有无该用户记录
        User queryUser = userMapper.selectUser(encryptUser.getUsername());
        if (queryUser != null) {
            //该用户已经存在,检查是否需要更新用户数据
            queryUser = queryUser.decryptUser();
            if (!queryUser.getUsername().equals(user.getUsername())
                    || !queryUser.getPassword().equals(user.getPassword())) {
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
        Introduction introduction = profileMapper.selectUserIntroduction(encryptUser.getUsername());
        if (introduction == null) {
            profileMapper.initUserIntroduction(encryptUser.getUsername());
        }
        Privacy privacy = privacyMapper.selectPrivacy(encryptUser.getUsername());
        if (privacy == null) {
            privacyMapper.initPrivacy(encryptUser.getUsername());
        }
    }
}
