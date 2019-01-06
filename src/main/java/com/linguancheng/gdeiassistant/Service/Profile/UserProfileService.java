package com.linguancheng.gdeiassistant.Service.Profile;

import com.aliyun.oss.OSSClient;
import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Introduction;
import com.linguancheng.gdeiassistant.Pojo.Entity.Profile;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender.GenderMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Service.Authenticate.AuthenticateService;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;

@Service
public class UserProfileService {

    @Autowired
    private AuthenticateService authenticateService;

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "genderMapper")
    private GenderMapper genderMapper;

    private String accessKeyID;

    private String accessKeySecret;

    private String endpoint;

    private static Map<Integer, String> genderMap;

    private static Map<Integer, String> genderOrientationMap;

    private static Map<Integer, String> facultyMap;

    @Autowired
    public AsyncRestTemplate asyncRestTemplate;

    @Resource(name = "genderMap")
    public void setGenderMap(Map<Integer, String> genderMap) {
        UserProfileService.genderMap = genderMap;
    }

    @Resource(name = "genderOrientationMap")
    public void setGenderOrientationMap(Map<Integer, String> genderOrientationMap) {
        UserProfileService.genderOrientationMap = genderOrientationMap;
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
        throw new DataNotExistException("查询的用户不存在");
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
        throw new DataNotExistException("查询的用户不存在");
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
     * 删除用户头像
     *
     * @param username
     */
    public void DeleteAvatar(String username) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + ".jpg")) {
            //删除文件
            ossClient.deleteObject("gdeiassistant-userdata", "avatar/" + username + ".jpg");
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
    @Transactional
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
    public void UpdateRegion(String username, String region, String state, String city) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setRegion(region);
            profile.setState(state);
            profile.setCity(city);
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
    }

    /**
     * 更新性别个人资料
     *
     * @param username
     * @param gender
     * @return
     */
    @Transactional
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
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
    }

    /**
     * 更新性取向个人资料
     *
     * @param username
     * @param genderOrientation
     * @return
     */
    public void UpdateGenderOrientation(String username, int genderOrientation) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setGenderOrientation(genderOrientation);
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
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
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
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
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
    }

    /**
     * 更新昵称个人资料
     *
     * @param username
     * @param kickname
     * @return
     */
    public void UpdateKickname(String username, String kickname) throws Exception {
        Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
        if (profile != null) {
            profile.setKickname(kickname);
            profileMapper.updateUserProfile(profile);
            return;
        }
        throw new DataNotExistException("查询的用户不存在");
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
     * 获取性取向字典
     *
     * @return
     */
    public static Map getGenderOrientationMap() {
        return genderOrientationMap;
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
