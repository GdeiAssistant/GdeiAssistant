package com.linguancheng.gdeiassistant.Service.Dating;

import com.aliyun.oss.OSSClient;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingMessage;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingPick;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingProfile;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Dating.DatingMapper;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class DatingService {

    @Resource(name = "datingMapper")
    private DatingMapper datingMapper;

    private String accessKeyID;

    private String accessKeySecret;

    private String endpoint;

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

    private Log log = LogFactory.getLog(DatingService.class);

    /**
     * 根据ID查找卖室友详细信息
     *
     * @param id
     * @return
     */
    public BaseResult<DatingProfile, DataBaseResultEnum> QueryDatingProfile(Integer id) {
        BaseResult<DatingProfile, DataBaseResultEnum> result = new BaseResult<>();
        try {
            DatingProfile datingProfile = datingMapper.selectDatingProfileById(id);
            if (datingProfile != null) {
                datingProfile.setUsername(StringEncryptUtils.decryptString(datingProfile.getUsername()));
                result.setResultType(DataBaseResultEnum.SUCCESS);
                result.setResultData(datingProfile);
            } else {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            }
        } catch (Exception e) {
            log.error("根据ID查询卖室友详细信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查找卖室友信息列表
     *
     * @param start
     * @param size
     * @return
     */
    public BaseResult<List<DatingProfile>, DataBaseResultEnum> QueryDatingProfile(Integer start, Integer size
            , Integer area) {
        BaseResult<List<DatingProfile>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<DatingProfile> list = datingMapper.selectDatingProfilePage(start, size, area);
            for (DatingProfile datingProfile : list) {
                datingProfile.setQq("请进入详情页查看");
                datingProfile.setWechat("请进入详情页查看");
                datingProfile.setUsername(StringEncryptUtils.decryptString(datingProfile.getUsername()));
            }
            result.setResultType(DataBaseResultEnum.SUCCESS);
            result.setResultData(list);
        } catch (Exception e) {
            log.error("分页查询卖室友信息列表异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 更新卖室友信息
     *
     * @param datingProfile
     * @return
     */
    public DataBaseResultEnum UpdateDatingProfile(DatingProfile datingProfile) {
        try {
            datingMapper.updateDatingProfile(datingProfile);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新卖室友信息异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新卖室友信息状态
     *
     * @param id
     * @param state
     * @return
     */
    public DataBaseResultEnum UpdateDatingProfileState(Integer id, Integer state) {
        try {
            datingMapper.updateDatingProfileState(id, state);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新卖室友信息状态异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 添加卖室友信息
     *
     * @param username
     * @param datingProfile
     * @return
     */
    public BaseResult<Integer, DataBaseResultEnum> AddDatingProfile(String username, DatingProfile datingProfile) {
        BaseResult<Integer, DataBaseResultEnum> result = new BaseResult<>();
        try {
            datingProfile.setUsername(StringEncryptUtils.encryptString(username));
            datingMapper.insertDatingProfile(datingProfile);
            result.setResultType(DataBaseResultEnum.SUCCESS);
            result.setResultData(datingProfile.getProfileId());
        } catch (Exception e) {
            log.error("添加卖室友信息异常" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 上传卖室友图片
     *
     * @param id
     * @param inputStream
     */
    public void UploadPicture(int id, InputStream inputStream) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "dating/" + id + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 查找撩一下记录
     *
     * @param profileId
     * @param username
     * @return
     */
    public BaseResult<DatingPick, DataBaseResultEnum> QueryDatingPick(Integer profileId, String username) {
        BaseResult<DatingPick, DataBaseResultEnum> result = new BaseResult<>();
        try {
            DatingPick datingPick = datingMapper.selectDatingPick(profileId
                    , StringEncryptUtils.encryptString(username));
            if (datingPick == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                datingPick.setUsername(StringEncryptUtils
                        .decryptString(datingPick.getUsername()));
                result.setResultData(datingPick);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("查找撩一下记录异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查找撩一下记录
     *
     * @param id
     * @return
     */
    public BaseResult<DatingPick, DataBaseResultEnum> QueryDatingPickById(Integer id) {
        BaseResult<DatingPick, DataBaseResultEnum> result = new BaseResult<>();
        try {
            DatingPick datingPick = datingMapper.selectDatingPickById(id);
            if (datingPick != null) {
                datingPick.setUsername(StringEncryptUtils.decryptString(datingPick.getUsername()));
                datingPick.getDatingProfile().setUsername(StringEncryptUtils
                        .decryptString(datingPick.getDatingProfile().getUsername()));
                result.setResultType(DataBaseResultEnum.SUCCESS);
                result.setResultData(datingPick);
            } else {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            }
        } catch (Exception e) {
            log.error("根据ID查找撩一下记录异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 添加撩一下记录
     *
     * @param datingPick
     * @return
     */
    public DataBaseResultEnum AddDatingPick(String username, DatingPick datingPick) {
        try {
            datingPick.setUsername(StringEncryptUtils.encryptString(username));
            datingMapper.insertDatingPick(datingPick);
            //创建卖室友通知
            DatingProfile datingProfile = datingMapper.selectDatingProfileById(datingPick
                    .getDatingProfile().getProfileId());
            DatingMessage datingMessage = new DatingMessage();
            datingMessage.setUsername(datingProfile.getUsername());
            datingMessage.setDatingPick(datingPick);
            datingMessage.setType(0);
            datingMessage.setState(0);
            datingMapper.insertDatingMessage(datingMessage);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("添加撩一下记录异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新撩一下状态
     * 0为被撩者未处理
     * 1为接受
     * 2为拒绝
     *
     * @param id
     * @param state
     * @return
     */
    public DataBaseResultEnum UpdateDatingPickState(Integer id, Integer state) {
        try {
            datingMapper.updateDatingPickState(id, state);
            DatingPick datingPick = datingMapper.selectDatingPickById(id);
            DatingMessage datingMessage = new DatingMessage();
            datingMessage.setUsername(datingPick.getUsername());
            datingMessage.setType(1);
            datingMessage.setDatingPick(datingPick);
            datingMessage.setState(0);
            datingMapper.insertDatingMessage(datingMessage);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新撩一下状态异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 分页查找用户卖室友消息列表
     *
     * @param username
     * @param start
     * @param size
     * @return
     */
    public BaseResult<List<DatingMessage>, DataBaseResultEnum> QueryUserDatingMessage(String username
            , Integer start, Integer size) {
        BaseResult<List<DatingMessage>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<DatingMessage> list = datingMapper.selectUserDatingMessagePage(StringEncryptUtils
                    .encryptString(username), start, size);
            for (DatingMessage datingMessage : list) {
                datingMessage.setUsername(StringEncryptUtils.decryptString(datingMessage.getUsername()));
                datingMessage.getDatingPick().setUsername(StringEncryptUtils
                        .decryptString(datingMessage.getDatingPick().getUsername()));
                datingMessage.getDatingPick().getDatingProfile().setUsername(StringEncryptUtils
                        .decryptString(datingMessage.getDatingPick().getDatingProfile().getUsername()));
            }
            result.setResultType(DataBaseResultEnum.SUCCESS);
            result.setResultData(list);
        } catch (Exception e) {
            log.error("分页查找用户卖室友消息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询用户未读的通知消息数量
     *
     * @param username
     * @return
     */
    public BaseResult<Integer, DataBaseResultEnum> QueryUserUnReadDatingMessageCount(String username) {
        BaseResult<Integer, DataBaseResultEnum> result = new BaseResult<>();
        try {
            Integer count = datingMapper.selectUserUnReadDatingMessageCount(StringEncryptUtils
                    .encryptString(username));
            result.setResultType(DataBaseResultEnum.SUCCESS);
            result.setResultData(count);
        } catch (Exception e) {
            log.error("查询用户卖室友未阅读消息数量异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 添加卖室友通知消息
     *
     * @param datingMessage
     * @return
     */
    public DataBaseResultEnum AddDatingMessage(DatingMessage datingMessage) {
        try {
            datingMessage.setUsername(StringEncryptUtils.encryptString(datingMessage.getUsername()));
            datingMapper.insertDatingMessage(datingMessage);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("添加卖室友通知消息异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 更新卖室友通知消息阅读状态
     *
     * @param id
     * @param state
     * @return
     */
    public DataBaseResultEnum UpdateDatingMessageState(Integer id, Integer state) {
        try {
            datingMapper.updateDatingMessageState(id, state);
            return DataBaseResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新卖室友通知消息阅读状态异常：" , e);
            return DataBaseResultEnum.ERROR;
        }
    }

    /**
     * 获取卖室友信息照片
     *
     * @param id
     * @return
     */
    public String GetDatingProfilePictureURL(int id) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //检查图片是否存在
        if (ossClient.doesObjectExist("gdeiassistant-userdata", "dating/" + id + ".jpg")) {
            //设置过期时间30分钟
            Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
            // 生成URL
            String url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "dating/" + id + ".jpg", expiration)
                    .toString().replace("http", "https");
            ossClient.shutdown();
            return url;
        }
        return null;
    }
}
