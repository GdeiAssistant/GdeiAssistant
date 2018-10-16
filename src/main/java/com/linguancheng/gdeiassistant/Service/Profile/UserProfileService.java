package com.linguancheng.gdeiassistant.Service.Profile;

import com.aliyun.oss.OSSClient;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.TransactionException;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender.GenderMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Profile.ProfileMapper;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.AuthorProfile;
import com.linguancheng.gdeiassistant.Pojo.Entity.Introduction;
import com.linguancheng.gdeiassistant.Pojo.Entity.Profile;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

@Service
public class UserProfileService {

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

    private Log log = LogFactory.getLog(UserProfileService.class);

    /**
     * 获取用户的作者简要资料
     *
     * @param username
     * @return
     */
    public BaseResult<AuthorProfile, DataBaseResultEnum> GetUserAuthorProfile(String username) {
        BaseResult<AuthorProfile, DataBaseResultEnum> result = new BaseResult<>();
        try {
            AuthorProfile authorProfile = profileMapper.selectAuthorProfile(StringEncryptUtils.encryptString(username));
            if (authorProfile != null) {
                authorProfile.setAvatarURL(GetUserAvatar(username));
                authorProfile.setUsername(username);
                result.setResultData(authorProfile);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            } else {
                result.setResultType(DataBaseResultEnum.INCORRECT_USERNAME);
            }
        } catch (Exception e) {
            log.error("获取用户作者简要资料异常：", e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 获取用户个人资料
     *
     * @param username
     * @return
     */
    public BaseResult<Profile, DataBaseResultEnum> GetUserProfile(String username) {
        BaseResult<Profile, DataBaseResultEnum> result = new BaseResult<>();
        try {
            User user = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (!user.getState().equals(-1)) {
                Profile profile = profileMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
                if (profile != null) {
                    if (profile.getGender() != null && profile.getGender().equals(3)) {
                        profile.setCustomGenderName(genderMapper.selectCustomGender(StringEncryptUtils.encryptString(username)));
                    }
                    result.setResultType(DataBaseResultEnum.SUCCESS);
                    profile.setUsername(StringEncryptUtils.decryptString(profile.getUsername()));
                    result.setResultData(profile);
                } else {
                    result.setResultType(DataBaseResultEnum.INCORRECT_USERNAME);
                }
            } else {
                result.setResultType(DataBaseResultEnum.INCORRECT_USERNAME);
            }
        } catch (Exception e) {
            log.error("获取用户个人资料异常：", e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 获取用户个人简介
     *
     * @param username
     * @return
     */
    public BaseResult<String, DataBaseResultEnum> GetUserIntroduction(String username) {
        BaseResult<String, DataBaseResultEnum> result = new BaseResult<>();
        try {
            Introduction introduction = profileMapper.selectUserIntroduction(StringEncryptUtils.encryptString(username));
            if (introduction == null) {
                profileMapper.initUserIntroduction(StringEncryptUtils.encryptString(username));
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                if (StringUtils.isBlank(introduction.getIntroductionContent())) {
                    result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
                } else {
                    result.setResultData(introduction.getIntroductionContent());
                    result.setResultType(DataBaseResultEnum.SUCCESS);
                }
            }
        } catch (Exception e) {
            log.error("获取用户个人简介异常：", e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
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
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/common/" + username + ".jpg")) {
            //设置过期时间10分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            // 生成URL
            url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "avatar/common/" + username + ".jpg", expiration).toString().replace("http", "https");
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
        ossClient.putObject("gdeiassistant-userdata", "avatar/common/" + username + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 删除用户头像
     *
     * @param username
     */
    public void DeleteAvatar(String username) {
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/common/" + username + ".jpg")) {
            //删除文件
            ossClient.deleteObject("gdeiassistant-userdata", "avatar/common/" + username + ".jpg");
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
    public DataBaseResultEnum UpdateIntroduction(String username, String introductionContent) {
        try {
            Introduction introduction = profileMapper.selectUserIntroduction(StringEncryptUtils.encryptString(username));
            if (introduction != null) {
                profileMapper.updateUserIntroduction(StringEncryptUtils.encryptString(username), introductionContent);
                return DataBaseResultEnum.SUCCESS;
            } else {
                profileMapper.initUserIntroduction(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserIntroduction(StringEncryptUtils.encryptString(username), introductionContent);
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户个人简介异常：", e);
            return DataBaseResultEnum.ERROR;
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
    public DataBaseResultEnum UpdateRegion(String username, String region
            , String state, String city) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
                Profile profile = new Profile();
                profile.setRegion(region);
                profile.setState(state);
                profile.setCity(city);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserLocation(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户所在地异常：", e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新性别个人资料
     *
     * @param username
     * @param gender
     * @return
     */
    @Transactional
    public DataBaseResultEnum UpdateGender(String username, int gender, String customGenderName) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
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
                Profile profile = new Profile();
                profile.setGender(gender);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserProfile(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户性别异常：", e);
            throw new TransactionException("更新用户性别异常，进行事务回滚");
        }
    }

    /**
     * 更新性取向个人资料
     *
     * @param username
     * @param genderOrientation
     * @return
     */
    public DataBaseResultEnum UpdateGenderOrientation(String username, int genderOrientation) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
                Profile profile = new Profile();
                profile.setGenderOrientation(genderOrientation);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserProfile(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户性取向异常：", e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新院系个人资料
     *
     * @param username
     * @param faculty
     * @return
     */
    public DataBaseResultEnum UpdateFaculty(String username, int faculty) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
                Profile profile = new Profile();
                profile.setFaculty(faculty);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserProfile(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户院系异常：", e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新专业个人资料
     *
     * @param username
     * @param major
     * @return
     */
    public DataBaseResultEnum UpdateMajor(String username, String major) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
                Profile profile = new Profile();
                profile.setMajor(major);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserProfile(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户专业异常：", e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新昵称个人资料
     *
     * @param username
     * @param kickname
     * @return
     */
    public DataBaseResultEnum UpdateKickname(String username, String kickname) {
        try {
            User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (queryUser != null) {
                Profile profile = new Profile();
                profile.setKickname(kickname);
                profile.setUsername(StringEncryptUtils.encryptString(username));
                profileMapper.updateUserProfile(profile);
                return DataBaseResultEnum.SUCCESS;
            }
            return DataBaseResultEnum.INCORRECT_USERNAME;
        } catch (Exception e) {
            log.error("更新用户昵称异常：", e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 定时同步用户个人资料真实姓名
     */
    @Scheduled(cron = "0 0 6 * * ?")
    public void InitUserRealName() {
        try {
            List<User> list = profileMapper.selectUninitializedUsername();
            //设置线程信号量，限制最大同时查询的线程数为10
            Semaphore semaphore = new Semaphore(10);
            for (User user : list) {
                if (!user.getState().equals(-1)) {
                    User decryptUser = user.decryptUser();
                    HttpHeaders httpHeaders = new HttpHeaders();
                    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                    params.add("username", user.getUsername());
                    params.add("password", user.getPassword());
                    semaphore.acquire();
                    ListenableFuture<ResponseEntity<DataJsonResult<String>>> future = asyncRestTemplate
                            .exchange("https://www.gdeiassistant.cn/rest/api/profile/realname"
                                    , HttpMethod.POST, new HttpEntity<>(params, httpHeaders)
                                    , new ParameterizedTypeReference<DataJsonResult<String>>() {
                                    });
                    future.addCallback(new ListenableFutureCallback<ResponseEntity<DataJsonResult<String>>>() {

                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("同步用户个人资料真实姓名异常：", ex);
                            semaphore.release();
                        }

                        @Override
                        public void onSuccess(ResponseEntity<DataJsonResult<String>> result) {
                            try {
                                if (result.getBody().isSuccess()) {
                                    profileMapper.updateRealName(decryptUser.getUsername(), result.getBody().getData());
                                } else {
                                    log.error("同步用户个人资料真实姓名异常：" + result.getBody().getErrorMessage());
                                }
                            } catch (Exception e) {
                                log.error("同步用户个人资料真实姓名异常：", e);
                            } finally {
                                semaphore.release();
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("同步用户个人资料真实姓名异常：", e);
        }
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
