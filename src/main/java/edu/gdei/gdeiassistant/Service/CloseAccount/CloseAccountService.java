package edu.gdei.gdeiassistant.Service.CloseAccount;

import edu.gdei.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import edu.gdei.gdeiassistant.Exception.CloseAccountException.UserStateErrorException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import edu.gdei.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Cet.CetMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Dating.DatingMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Ershou.ErshouMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender.GenderMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.WechatUser.WechatUserMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.YiBanUser.YiBanUserMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Close.CloseMapper;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CloseAccountService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ErshouMapper ershouMapper;

    @Autowired
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private DatingMapper datingMapper;

    @Autowired
    private CetMapper cetMapper;

    @Autowired
    private GenderMapper genderMapper;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private YiBanUserMapper yiBanUserMapper;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Autowired
    private CloseMapper closeMapper;

    /**
     * 删除用户账号
     *
     * @param username
     * @param password
     * @throws Exception
     */
    @Transactional("appTransactionManager")
    public void CloseAccount(String username, String password) throws Exception {
        //检查用户账号状态
        User user = userMapper.selectUser(StringEncryptUtils.encryptString(username)).decryptUser();
        if (user == null || !user.getState().equals(0)) {
            //若账号状态异常，则抛出异常
            throw new UserStateErrorException("用户账号状态异常");
        }
        if (!user.getPassword().equals(password)) {
            //账号密码错误
            throw new PasswordIncorrectException("用户账号密码不匹配");
        }
        //检查有无待处理的社区功能信息
        List<ErshouItem> ershouItemList = ershouMapper
                .selectItemsByUsername(StringEncryptUtils.encryptString(username));
        for (ErshouItem ershouItem : ershouItemList) {
            if (ershouItem.getState().equals(1)) {
                throw new ItemAvailableException("用户有待处理的社区功能信息");
            }
        }
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(StringEncryptUtils.encryptString(username));
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(1)) {
                throw new ItemAvailableException("用户有待处理的社区功能信息");
            }
        }
        List<DatingProfile> datingProfileList = datingMapper
                .selectDatingProfileByUsername(StringEncryptUtils.encryptString(username));
        for (DatingProfile datingProfile : datingProfileList) {
            if (datingProfile.getState().equals(1)) {
                throw new ItemAvailableException("用户有待处理的社区功能信息");
            }
        }

        //开始进行账号关闭事务

        //删除四六级准考证号
        CetNumber cetNumber = cetMapper
                .selectNumber(StringEncryptUtils.encryptString(username));
        if (cetNumber != null) {
            cetMapper.updateNumber(StringEncryptUtils.encryptString(username), null);
        }
        //删除自定义性别
        genderMapper.deleteCustomGender(StringEncryptUtils.encryptString(username));
        //删除教务缓存信息
        gradeDao.removeGrade(username);
        scheduleDao.removeSchedule(username);
        //移除易班和微信绑定状态
        wechatUserMapper.resetWechatUser(StringEncryptUtils.encryptString(username));
        yiBanUserMapper.resetYiBanUser(StringEncryptUtils.encryptString(username));
        //删除用户资料信息
        profileMapper.resetUserProfile(StringEncryptUtils.encryptString(username), "广东二师助手用户");
        profileMapper.resetUserIntroduction(StringEncryptUtils.encryptString(username));
        //重置用户隐私配置
        privacyMapper.resetPrivacy(StringEncryptUtils.encryptString(username));
        //删除用户头像
        userProfileService.DeleteAvatar(username);
        //删除用户账号信息
        Integer count = userMapper.selectDeletedUserCount("del_"
                + StringEncryptUtils.SHA1HexString(username).substring(0, 15));
        count = count == null ? 0 : count;
        userMapper.closeUser("del_" + StringEncryptUtils.SHA1HexString(username)
                .substring(0, 15) + "_" + count, StringEncryptUtils.encryptString(username));
        //保存注销日志
        SaveCloseLog(username, count);
    }

    /**
     * 记录账号关闭日志
     *
     * @param username
     * @param count
     */
    private void SaveCloseLog(String username, int count) {
        CloseLog closeLog = new CloseLog();
        closeLog.setUsername(username);
        closeLog.setResetname("del_" + StringEncryptUtils.SHA1HexString(username)
                .substring(0, 15) + "_" + count);
        closeMapper.insertCloseLog(closeLog);
    }
}
