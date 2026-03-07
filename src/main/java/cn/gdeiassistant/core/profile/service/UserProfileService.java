package cn.gdeiassistant.core.profile.service;

import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException;
import cn.gdeiassistant.common.pojo.Entity.Introduction;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.profile.converter.UserProfileMapper;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserProfileService {

    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Autowired
    private UserProfileMapper userProfileMapper;

    @Resource(name = "professionMap")
    public void setProfessionMap(Map<Integer, String> professionMap) {
        UserProfileService.professionMap = professionMap;
    }

    private static Map<Integer, String> professionMap;

    private static Map<Integer, String> facultyMap;

    private static Map<Integer, String> degreeMap;

    /**
     * 获取职业字典
     *
     * @return
     */
    public static Map getProfessionMap() {
        return professionMap;
    }

    @Resource(name = "degreeMap")
    public void setDegreeMap(Map<Integer, String> degreeMap) {
        UserProfileService.degreeMap = degreeMap;
    }

    @Resource(name = "facultyMap")
    public void setFacultyMap(Map<Integer, String> facultyMap) {
        UserProfileService.facultyMap = facultyMap;
    }

    @Autowired
    private R2StorageService r2StorageService;

    /**
     * 获取当前用户个人资料
     */
    public ProfileVO getSelfUserProfile(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            // 会话已失效，触发统一 Token 过期处理（401 + 自动登出）
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        return getUserProfile(user.getUsername());
    }

    /**
     * 获取指定用户名用户个人资料
     */
    public ProfileVO getUserProfile(String username) throws Exception {
        ProfileEntity entity = profileMapper.selectUserProfile(username);
        if (entity != null) {
            return userProfileMapper.toVO(entity);
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取当前用户个人简介
     *
     * @param username
     * @return
     */
    public Introduction getOtherUserIntroduction(String username) throws UserNotExistException {
        Introduction introduction = profileMapper.selectUserIntroduction(username);
        if (introduction != null) {
            return introduction;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取当前用户个人简介
     *
     * @param sessionId
     * @return
     */
    public Introduction getSelfUserIntroduction(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        return getOtherUserIntroduction(user.getUsername());
    }

    /**
     * 获取当前用户的头像图片URL
     *
     * @param sessionId
     * @return
     */
    public String getSelfUserAvatar(String sessionId) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        return getOtherUserAvatar(user.getUsername());
    }

    /**
     * 获取其他用户的头像图片URL
     *
     * @param username
     * @return
     */
    public String getOtherUserAvatar(String username) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "avatar/"
                + username + ".jpg", 30, TimeUnit.MINUTES);
    }

    /**
     * 获取当前用户的头像高清图片URL
     *
     * @param sessionId
     * @return
     */
    public String getSelfUserHighDefinitionAvatar(String sessionId) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        return getOtherUserHighDefinitionAvatar(user.getUsername());
    }

    /**
     * 获取其他用户的头像高清图片URL
     *
     * @param username
     * @return
     */
    public String getOtherUserHighDefinitionAvatar(String username) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "avatar/"
                + username + "_hd.jpg", 30, TimeUnit.MINUTES);
    }

    /**
     * 更新用户头像
     *
     * @param sessionId
     * @param inputStream
     * @return
     */
    public void updateAvatar(String sessionId, InputStream inputStream) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        r2StorageService.uploadObject("gdeiassistant-userdata", "avatar/"
                + user.getUsername() + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            // 关闭头像上传流失败仅记录日志，不中断业务流程
            org.slf4j.LoggerFactory.getLogger(UserProfileService.class)
                    .error("关闭头像上传输入流失败", e);
        }
    }

    /**
     * 更新用户高清头像
     *
     * @param sessionId
     * @param inputStream
     */
    public void updateHighDefinitionAvatar(String sessionId, InputStream inputStream) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        r2StorageService.uploadObject("gdeiassistant-userdata", "avatar/"
                        + user.getUsername() + "_hd.jpg"
                , inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            org.slf4j.LoggerFactory.getLogger(UserProfileService.class)
                    .error("关闭高清头像上传输入流失败", e);
        }
    }

    public void updateAvatarByObjectKey(String sessionId, String objectKey) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "avatar/" + user.getUsername() + ".jpg");
    }

    public void updateHighDefinitionAvatarByObjectKey(String sessionId, String objectKey) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "avatar/" + user.getUsername() + "_hd.jpg");
    }

    /**
     * 删除用户头像
     *
     * @param sessionId
     */
    public void deleteAvatar(String sessionId) throws TokenExpiredException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new cn.gdeiassistant.common.exception.TokenValidException.TokenExpiredException("登录凭证已过期，请重新登录");
        }
        r2StorageService.deleteObject("gdeiassistant-userdata", "avatar/" + user.getUsername() + ".jpg");
        r2StorageService.deleteObject("gdeiassistant-userdata", "avatar/" + user.getUsername() + "_hd.jpg");
    }

    /**
     * 更新个人简介
     *
     * @param sessionId
     * @param introductionContent
     * @return
     */
    @Transactional("appTransactionManager")
    public void updateIntroduction(String sessionId, String introductionContent) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        Introduction introduction = profileMapper.selectUserIntroduction(user.getUsername());
        if (introduction != null) {
            profileMapper.updateUserIntroduction(user.getUsername(), introductionContent);
        } else {
            profileMapper.initUserIntroduction(user.getUsername());
            profileMapper.updateUserIntroduction(user.getUsername(), introductionContent);
        }
    }

    /**
     * 更新用户所在地
     *
     * @param sessionId
     * @param region
     * @param state
     * @param city
     * @return
     */
    public void updateLocation(String sessionId, String region, String state, String city) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setLocationRegion(region);
            entity.setLocationState(state);
            entity.setLocationCity(city);
            profileMapper.updateLocation(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新用户家乡
     *
     * @param sessionId
     * @param region
     * @param state
     * @param city
     * @throws Exception
     */
    public void updateHometown(String sessionId, String region, String state, String city) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setHometownRegion(region);
            entity.setHometownState(state);
            entity.setHometownCity(city);
            profileMapper.updateHometown(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新生日日期
     *
     * @param sessionId
     * @param year
     * @param month
     * @param date
     * @return
     */
    public void updateBirthday(String sessionId, int year, int month, int date) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            Date birthday = Date.from(LocalDate.of(year, month, date)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            entity.setBirthday(birthday);
            profileMapper.updateBirthday(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 重置生日日期
     *
     * @param sessionId
     * @throws Exception
     */
    public void resetBirthday(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setBirthday(null);
            profileMapper.updateBirthday(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新院系个人资料
     *
     * @param sessionId
     * @param faculty
     * @return
     */
    public void updateFaculty(String sessionId, int faculty) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setFaculty(faculty);
            profileMapper.updateFaculty(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新专业个人资料
     *
     * @param sessionId
     * @param major
     * @return
     */
    public void updateMajor(String sessionId, String major) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setMajor(major);
            profileMapper.updateMajor(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新入学年份
     *
     * @param sessionId
     * @param enrollment
     * @throws Exception
     */
    public void updateEnrollment(String sessionId, int enrollment) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setEnrollment(enrollment);
            profileMapper.updateEnrollment(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 重置入学年份信息
     *
     * @param sessionId
     * @throws Exception
     */
    public void resetEnrollment(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setEnrollment(null);
            profileMapper.updateEnrollment(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新昵称个人资料
     *
     * @param sessionId
     * @param nickname
     * @return
     */
    public void updateNickname(String sessionId, String nickname) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (user == null) {
            throw new TokenExpiredException("登录凭证已过期，请重新登录");
        }
        ProfileEntity entity = profileMapper.selectUserProfile(user.getUsername());
        if (entity != null) {
            entity.setNickname(nickname);
            profileMapper.updateNickname(entity);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取学历字典
     *
     * @return
     */
    public static Map getDegreeMap() {
        return degreeMap;
    }

    /**
     * 获取院系字典
     *
     * @return
     */
    public static Map getFacultyMap() {
        return facultyMap;
    }
}
