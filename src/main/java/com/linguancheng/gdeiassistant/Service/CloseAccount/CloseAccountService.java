package com.linguancheng.gdeiassistant.Service.CloseAccount;

import com.linguancheng.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import com.linguancheng.gdeiassistant.Exception.CloseAccountException.UserStateErrorException;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Pojo.CetQuery.CetNumberQueryResult;
import com.linguancheng.gdeiassistant.Pojo.Entity.*;
import com.linguancheng.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import com.linguancheng.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Cet.CetMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Dating.DatingMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Ershou.ErshouMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender.GenderMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Privacy.PrivacyMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.WechatUser.WechatUserMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.YiBanUser.YiBanUserMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Close.CloseMapper;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/25
 */
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
    @Transactional
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
        CetNumberQueryResult cetNumberQueryResult = cetMapper
                .selectNumber(StringEncryptUtils.encryptString(username));
        if (cetNumberQueryResult != null) {
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
        //记录账号关闭日志
        CloseLog closeLog = new CloseLog();
        closeLog.setUsername(username);
        closeLog.setResetname("del_" + StringEncryptUtils.SHA1HexString(username)
                .substring(0, 15) + "_" + count);
        closeLog.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        closeMapper.insertCloseLog(closeLog);
    }
}
