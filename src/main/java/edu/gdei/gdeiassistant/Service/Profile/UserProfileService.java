package edu.gdei.gdeiassistant.Service.Profile;

import com.aliyun.oss.OSSClient;
import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.Introduction;
import edu.gdei.gdeiassistant.Pojo.Entity.Profile;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Gender.GenderMapper;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;

@Service
public class UserProfileService {

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Resource(name = "genderMapper")
    private GenderMapper genderMapper;

    @Resource(name = "professionMap")
    public void setProfessionMap(Map<Integer, String> professionMap) {
        UserProfileService.professionMap = professionMap;
    }

    private String accessKeyID;

    private String accessKeySecret;

    private String endpoint;

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

    @Value("#{propertiesReader['oss.accessKeySecret']}")
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Value("#{propertiesReader['oss.accessKeyID']}")
    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    @Value("#{propertiesReader['oss.endpoint']}")
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * 获取用户个人资料
     *
     * @param username
     * @return
     */
    public Profile GetUserProfile(String username) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            if (profile.getGender() != null && profile.getGender().equals(3)) {
                profile.setCustomGenderName(genderMapper.selectCustomGender(StringEncryptUtils.encryptString(username)));
            }
            profile.setUsername(StringEncryptUtils.decryptString(profile.getUsername()));
            return profile;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取用户个人简介
     *
     * @param username
     * @return
     */
    public Introduction GetUserIntroduction(String username) throws Exception {
        Introduction introduction = profileMapper.selectUserIntroduction(StringEncryptUtils
                .encryptString(username));
        if (introduction != null) {
            return introduction;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 获取用户的头像图片URL
     *
     * @param username
     * @return
     */
    public String GetUserAvatar(String username) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        String url = "";
        //检查自定义头像图片是否存在
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + ".jpg")) {
            //设置过期时间10分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            // 生成URL
            url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "avatar/" + username + ".jpg", expiration).toString().replace("http", "https");
        }
        ossClient.shutdown();
        return url;
    }

    /**
     * 获取用户的头像高清图片URL
     *
     * @param username
     * @return
     */
    public String GetUserHighDefinitionAvatar(String username) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        String url = "";
        //检查自定义头像图片是否存在
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg")) {
            //设置过期时间10分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            // 生成URL
            url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg", expiration).toString().replace("http", "https");
        }
        ossClient.shutdown();
        return url;
    }

    /**
     * 更新用户头像
     *
     * @param username
     * @param inputStream
     * @return
     */
    public void UpdateAvatar(String username, InputStream inputStream) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "avatar/" + username + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 更新用户高清头像
     *
     * @param username
     * @param inputStream
     */
    public void UpdateHighDefinitionAvatar(String username, InputStream inputStream) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 删除用户头像
     *
     * @param username
     */
    public void DeleteAvatar(String username) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + ".jpg")) {
            //删除头像文件
            ossClient.deleteObject("gdeiassistant-userdata", "avatar/" + username + ".jpg");
        }
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg")) {
            //删除高清头像文件
            ossClient.deleteObject("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg");
        }
        ossClient.shutdown();
    }

    /**
     * 更新个人简介
     *
     * @param username
     * @param introductionContent
     * @return
     */
    @Transactional("appTransactionManager")
    public void UpdateIntroduction(String username, String introductionContent) throws Exception {
        Introduction introduction = profileMapper.selectUserIntroduction(StringEncryptUtils.encryptString(username));
        if (introduction != null) {
            profileMapper.updateUserIntroduction(StringEncryptUtils.encryptString(username), introductionContent);
        } else {
            profileMapper.initUserIntroduction(StringEncryptUtils.encryptString(username));
            profileMapper.updateUserIntroduction(StringEncryptUtils.encryptString(username), introductionContent);
        }
    }

    /**
     * 更新用户所在地
     *
     * @param username
     * @param region
     * @param state
     * @param city
     * @return
     */
    public void UpdateLocation(String username, String region, String state, String city) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @param region
     * @param state
     * @param city
     * @throws Exception
     */
    public void UpdateHometown(String username, String region, String state, String city) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @param gender
     * @return
     */
    @Transactional("appTransactionManager")
    public void UpdateGender(String username, int gender, String customGenderName) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            //若选择传统性别，则清除自定义性别表记录
            if (gender != 3) {
                if (genderMapper.selectCustomGender(StringEncryptUtils.encryptString(username)) != null) {
                    genderMapper.deleteCustomGender(StringEncryptUtils.encryptString(username));
                }
            } else {
                //保存自定义性别
                if (genderMapper.selectCustomGender(StringEncryptUtils.encryptString(username)) != null) {
                    genderMapper.updateCustomGender(StringEncryptUtils.encryptString(username), customGenderName);
                } else {
                    genderMapper.insertCustomGender(StringEncryptUtils.encryptString(username), customGenderName);
                }
            }
            profile.setGender(gender);
            profileMapper.updateGender(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新生日日期
     *
     * @param username
     * @param year
     * @param month
     * @param date
     * @return
     */
    public void UpdateBirthday(String username, int year, int month, int date) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @throws Exception
     */
    public void ResetBirthday(String username) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setBirthday(null);
            profileMapper.updateBirthday(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新学历信息
     *
     * @param username
     * @param degree
     * @throws Exception
     */
    public void UpdateDegree(String username, int degree) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setDegree(degree);
            profileMapper.updateDegree(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新院系个人资料
     *
     * @param username
     * @param faculty
     * @return
     */
    public void UpdateFaculty(String username, int faculty) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @param major
     * @return
     */
    public void UpdateMajor(String username, String major) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @param enrollment
     * @throws Exception
     */
    public void UpdateEnrollment(String username, int enrollment) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @throws Exception
     */
    public void ResetEnrollment(String username) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
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
     * @param username
     * @param nickname
     * @return
     */
    public void UpdateNickname(String username, String nickname) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setNickname(nickname);
            profileMapper.updateNickname(profile);
            return;
        }
        throw new UserNotExistException("查询的用户不存在");
    }

    /**
     * 更新学校信息
     *
     * @param username
     * @param school
     * @param index
     * @throws Exception
     */
    public void UpdateSchool(String username, String school, int index) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            switch (index) {
                case 0:
                    //大专院校
                    profile.setColleges(school);
                    profileMapper.updateColleges(profile);
                    break;

                case 1:
                    //高中/职中
                    profile.setHighSchool(school);
                    profileMapper.updateHighSchool(profile);
                    break;

                case 2:
                    //初中
                    profile.setJuniorHighSchool(school);
                    profileMapper.updateJuniorHighSchool(profile);
                    break;

                case 3:
                    //小学
                    profile.setPrimarySchool(school);
                    profileMapper.updatePrimarySchool(profile);
                    break;
            }
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
     * 更新职业个人资料
     *
     * @param username
     * @param profession
     * @throws Exception
     */
    public void UpdateProfession(String username, int profession) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setProfession(profession);
            profileMapper.updateProfession(profile);
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
