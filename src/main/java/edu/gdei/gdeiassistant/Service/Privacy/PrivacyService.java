package edu.gdei.gdeiassistant.Service.Privacy;

import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.Privacy;
import edu.gdei.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import edu.gdei.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper;
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
        privacyMapper.updateLocation(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新家乡隐私设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateHometown(boolean state, String username) throws Exception {
        privacyMapper.updateHometown(state, StringEncryptUtils.encryptString(username));
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
     * 更新入学年份隐私设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateEnrollment(boolean state, String username) throws Exception {
        privacyMapper.updateEnrollment(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新年龄隐私设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateAge(boolean state, String username) throws Exception {
        privacyMapper.updateAge(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新学历隐私设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateDegree(boolean state, String username) throws Exception {
        privacyMapper.updateDegree(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新职业隐私设置
     *
     * @param state
     * @param username
     * @throws Exception
     */
    public void UpdateProfession(boolean state, String username) throws Exception {
        privacyMapper.updateProfession(state, StringEncryptUtils.encryptString(username));
    }

    /**
     * 更新学校隐私设置
     *
     * @param state
     * @param index
     * @param username
     * @throws Exception
     */
    public void UpdateSchool(boolean state, int index, String username) throws Exception {
        switch (index) {
            case 0:
                //高中/职中
                privacyMapper.updateHighSchool(state, StringEncryptUtils.encryptString(username));
                break;

            case 1:
                //初中
                privacyMapper.updateJuniorHighSchool(state, StringEncryptUtils.encryptString(username));
                break;

            case 2:
            default:
                //小学
                privacyMapper.updatePrimarySchool(state, StringEncryptUtils.encryptString(username));
                break;
        }
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
