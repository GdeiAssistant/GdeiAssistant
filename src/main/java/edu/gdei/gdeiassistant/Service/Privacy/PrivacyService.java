package edu.gdei.gdeiassistant.Service.Privacy;

import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import edu.gdei.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class PrivacyService {

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    /**
     * 获取用户隐私设置
     *
     * @param username
     * @return
     */
    public Privacy GetPrivacySetting(String username) throws Exception {
        Privacy privacy = privacyMapper.selectPrivacy(StringEncryptUtils
                .encryptString(username));
        if (privacy != null) {
            return privacy;
        }
        throw new UserNotExistException("当前用户不存在");
    }

    /**
     * 更新性别隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateGender(boolean state, String username) throws Exception {
        privacyMapper.updateGender(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新院系隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateFaculty(boolean state, String username) throws Exception {
        privacyMapper.updateFaculty(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新专业隐私配置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateMajor(boolean state, String username) throws Exception {
        privacyMapper.updateMajor(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新国家/地区隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateLocation(boolean state, String username) throws Exception {
        privacyMapper.updateRegion(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新个人简介隐私设置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateIntroduction(boolean state, String username) throws Exception {
        privacyMapper.updateIntroduction(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新教务信息缓存隐私配置
     *
     * @param state
     * @param username
     * @return
     */
    public void UpdateCache(boolean state, String username) throws Exception {
        privacyMapper.updateCache(state, StringEncryptUtils.encryptString(username));
        gradeDao.removeGrade(username);
        scheduleDao.removeSchedule(username);
    }

    /**
     * 更新个人主页搜索引擎收录设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateRobotsIndex(boolean state, String username) throws Exception {
        privacyMapper.updateRobotsIndex(state, StringEncryptUtils.encryptString(username));
    }
}
