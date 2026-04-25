package cn.gdeiassistant.core.privacy.service;

import cn.gdeiassistant.common.exception.CommonException.CacheClearException;
import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.AnonymizeUtils;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.privacy.converter.PrivacyConverter;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.privacy.pojo.vo.PrivacyVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class PrivacyService {

    private static final Logger logger = LoggerFactory.getLogger(PrivacyService.class);

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Autowired
    private PrivacyConverter privacyConverter;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    public PrivacyVO getSelfUserPrivacySetting(String sessionId) throws UserNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return getOtherUserPrivacySetting(user.getUsername());
    }

    public PrivacyVO getOtherUserPrivacySetting(String username) throws UserNotExistException {
        PrivacyEntity entity = privacyMapper.selectPrivacy(username);
        if (entity == null) throw new UserNotExistException("当前用户不存在");
        return privacyConverter.toVO(entity);
    }

    /**
     * 更新院系隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void updateFaculty(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateFaculty(state, user.getUsername());
    }

    /**
     * 更新专业隐私配置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void updateMajor(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateMajor(state, user.getUsername());
    }

    /**
     * 更新国家/地区隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void updateLocation(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateLocation(state, user.getUsername());
    }

    /**
     * 更新家乡隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void updateHometown(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateHometown(state, user.getUsername());
    }

    /**
     * 更新个人简介隐私设置
     *
     * @param state
     * @param sessionId
     * @return
     */
    public void updateIntroduction(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateIntroduction(state, user.getUsername());
    }

    /**
     * 更新入学年份隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void updateEnrollment(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateEnrollment(state, user.getUsername());
    }

    /**
     * 更新年龄隐私设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void updateAge(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateAge(state, user.getUsername());
    }

    /**
     * 更新教务信息缓存隐私配置。
     * MySQL 更新为主流程；MongoDB 缓存清理失败时抛出 CacheClearException，由 Controller 返回 206 部分成功。
     *
     * @param state
     * @param sessionId
     * @throws CacheClearException MySQL 已更新但 MongoDB 清理失败时抛出
     */
    public void updateCache(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateCache(state, user.getUsername());
        try {
            gradeDao.removeGrade(user.getUsername());
            scheduleDao.removeSchedule(user.getUsername());
        } catch (Exception e) {
            logger.warn("清理 MongoDB 缓存失败，MySQL 隐私设置已更新，username={}", AnonymizeUtils.maskUsername(user.getUsername()), e);
            throw new CacheClearException("清理教务缓存时遇到网络延迟，设置已保存，可能需要稍后生效。", e);
        }
    }

    /**
     * 更新个人主页搜索引擎收录设置
     *
     * @param state
     * @param sessionId
     * @throws Exception
     */
    public void updateRobotsIndex(boolean state, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        privacyMapper.updateRobotsIndex(state, user.getUsername());
    }
}
