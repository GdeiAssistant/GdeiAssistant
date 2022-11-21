package cn.gdeiassistant.Service.Privacy;

import cn.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.Pojo.Entity.Privacy;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import cn.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PrivacyService {

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    /**
     * 获取当前用户隐私设置
     *
     * @param sessionId
     * @return
     */
    public Privacy GetSelfUserPrivacySetting(String sessionId) throws UserNotExistException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return GetOtherUserPrivacySetting(user.getUsername());
    }

    /**
     * 获取其他用户隐私设置
     *
     * @param username
     * @return
     * @throws UserNotExistException
     */
    public Privacy GetOtherUserPrivacySetting(String username) throws UserNotExistException {
        Privacy privacy = privacyMapper.selectPrivacy(username);
        if (privacy != null) {
            return privacy;
        }
        throw new UserNotExistException("当前用户不存在");
    }

    /**
     * 更新性别隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateGender(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateGender(state, user.getUsername());
    }

    /**
     * 更新院系隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateFaculty(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateFaculty(state, user.getUsername());
    }

    /**
     * 更新专业隐私配置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateMajor(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateMajor(state, user.getUsername());
    }

    /**
     * 更新国家/地区隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateLocation(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateLocation(state, user.getUsername());
    }

    /**
     * 更新家乡隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void UpdateHometown(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateHometown(state, user.getUsername());
    }

    /**
     * 更新个人简介隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateIntroduction(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateIntroduction(state, user.getUsername());
    }

    /**
     * 更新入学年份隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void UpdateEnrollment(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateEnrollment(state, user.getUsername());
    }

    /**
     * 更新年龄隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void UpdateAge(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateAge(state, user.getUsername());
    }

    /**
     * 更新教务信息缓存隐私配置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void UpdateCache(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateCache(state, user.getUsername());
        gradeDao.removeGrade(user.getUsername());
        scheduleDao.removeSchedule(user.getUsername());
    }

    /**
     * 更新个人主页搜索引擎收录设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void UpdateRobotsIndex(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        privacyMapper.updateRobotsIndex(state, user.getUsername());
    }
}
