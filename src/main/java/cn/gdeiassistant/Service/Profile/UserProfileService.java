package cn.gdeiassistant.Service.Profile;

import cn.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.Pojo.Entity.Introduction;
import cn.gdeiassistant.Pojo.Entity.Profile;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.OSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
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

    @Resource(name = "professionMap")
    public void setProfessionMap(Map<Integer, String> professionMap) {
        UserProfileService.professionMap = professionMap;
    }

    private static Map<Integer, String> genderMap;

    private static Map<Integer, String> professionMap;

    private static Map<Integer, String> facultyMap;

    private static Map<Integer, String> degreeMap;

    @Autowired
    public AsyncRestTemplate asyncRestTemplate;

    @Resource(name = "genderMap")
    public void setGenderMap(Map<Integer, String> genderMap) {
        UserProfileService.genderMap = genderMap;
    }

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
    private OSSUtils ossUtils;

    /**
     * 获取用户个人资料
     *
     * @param sessionId
     * @return
     */
    public Profile GetUserProfile(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setUsername(profile.getUsername());
            return profile;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取用户个人简介
     *
     * @param sessionId
     * @return
     */
    public Introduction GetUserIntroduction(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Introduction introduction = profileMapper.selectUserIntroduction(user.getUsername());
        if (introduction != null) {
            return introduction;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取用户的头像图片URL
     *
     * @param sessionId
     * @return
     */
    public String GetUserAvatar(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "avatar/"
                + user.getUsername() + ".jpg", 30, TimeUnit.MINUTES);
    }

    /**
     * 获取用户的头像高清图片URL
     *
     * @param sessionId
     * @return
     */
    public String GetUserHighDefinitionAvatar(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "avatar/"
                + user.getUsername() + "_hd.jpg", 30, TimeUnit.MINUTES);
    }

    /**
     * 更新用户头像
     *
     * @param sessionId
     * @param inputStream
     * @return
     */
    public void UpdateAvatar(String sessionId, InputStream inputStream) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ossUtils.UploadOSSObject("gdeiassistant-userdata", "avatar/"
                + user.getUsername() + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 更新用户高清头像
     *
     * @param sessionId
     * @param inputStream
     */
    public void UpdateHighDefinitionAvatar(String sessionId, InputStream inputStream) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ossUtils.UploadOSSObject("gdeiassistant-userdata", "avatar/"
                        + user.getUsername() + "_hd.jpg"
                , inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除用户头像
     *
     * @param sessionId
     */
    public void DeleteAvatar(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ossUtils.DeleteOSSObject("gdeiassistant-userdata", "avatar/" + user.getUsername() + ".jpg");
        ossUtils.DeleteOSSObject("gdeiassistant-userdata", "avatar/" + user.getUsername() + "_hd.jpg");
    }

    /**
     * 更新个人简介
     *
     * @param sessionId
     * @param introductionContent
     * @return
     */
    @Transactional("appTransactionManager")
    public void UpdateIntroduction(String sessionId, String introductionContent) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
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
    public void UpdateLocation(String sessionId, String region, String state, String city) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setLocationRegion(region);
            profile.setLocationState(state);
            profile.setLocationCity(city);
            profileMapper.updateLocation(profile);
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
    public void UpdateHometown(String sessionId, String region, String state, String city) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setHometownRegion(region);
            profile.setHometownState(state);
            profile.setHometownCity(city);
            profileMapper.updateHometown(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新性别个人资料
     *
     * @param sessionId
     * @param gender
     * @return
     */
    @Transactional("appTransactionManager")
    public void UpdateGender(String sessionId, int gender, String customGenderName) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setGender(gender);
            profile.setCustomGenderName(customGenderName);
            profileMapper.updateGender(profile);
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
    public void UpdateBirthday(String sessionId, int year, int month, int date) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            Date birthday = Date.from(LocalDate.of(year, month, date)
                    .atStartOfDay(ZoneId.systemDefault()).toInstant());
            profile.setBirthday(birthday);
            profileMapper.updateBirthday(profile);
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
    public void ResetBirthday(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setBirthday(null);
            profileMapper.updateBirthday(profile);
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
    public void UpdateFaculty(String sessionId, int faculty) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setFaculty(faculty);
            profileMapper.updateFaculty(profile);
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
    public void UpdateMajor(String sessionId, String major) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setMajor(major);
            profileMapper.updateMajor(profile);
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
    public void UpdateEnrollment(String sessionId, int enrollment) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setEnrollment(enrollment);
            profileMapper.updateEnrollment(profile);
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
    public void ResetEnrollment(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setEnrollment(null);
            profileMapper.updateEnrollment(profile);
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
    public void UpdateNickname(String sessionId, String nickname) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile != null) {
            profile.setNickname(nickname);
            profileMapper.updateNickname(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取性别字典
     *
     * @return
     */
    public static Map getGenderMap() {
        return genderMap;
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
