package edu.gdei.gdeiassistant.Service.Dating;

import com.aliyun.oss.OSSClient;
import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingMessage;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingPick;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingProfile;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Dating.DatingMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.InputStream;
import java.util.Date;
import java.util.List;

@Deprecated
public class DatingService {

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

    private Logger logger = LoggerFactory.getLogger(DatingService.class);

    /**
     * 根据ID查找卖室友详细信息
     *
     * @param id
     * @return
     */
    public DatingProfile QueryDatingProfile(Integer id) throws WsgException, DataNotExistException {
        DatingProfile datingProfile = datingMapper.selectDatingProfileById(id);
        if (datingProfile != null) {
            datingProfile.setUsername(StringEncryptUtils.decryptString(datingProfile.getUsername()));
            return datingProfile;
        }
        throw new DataNotExistException("该卖室友信息不存在");
    }

    /**
     * 查找卖室友信息列表
     *
     * @param start
     * @param size
     * @return
     */
    public List<DatingProfile> QueryDatingProfile(Integer start, Integer size, Integer area) throws WsgException {
        List<DatingProfile> list = datingMapper.selectDatingProfilePage(start, size, area);
        for (DatingProfile datingProfile : list) {
            datingProfile.setQq("请进入详情页查看");
            datingProfile.setWechat("请进入详情页查看");
            datingProfile.setUsername(StringEncryptUtils.decryptString(datingProfile.getUsername()));
        }
        return list;
    }

    /**
     * 更新卖室友信息
     *
     * @param datingProfile
     * @return
     */
    public void UpdateDatingProfile(DatingProfile datingProfile) {
        datingMapper.updateDatingProfile(datingProfile);
    }

    /**
     * 更新卖室友信息状态
     *
     * @param id
     * @param state
     * @return
     */
    public void UpdateDatingProfileState(Integer id, Integer state) {
        datingMapper.updateDatingProfileState(id, state);
    }

    /**
     * 添加卖室友信息
     *
     * @param username
     * @param datingProfile
     * @return
     */
    public Integer AddDatingProfile(String username, DatingProfile datingProfile) throws WsgException {
        datingProfile.setUsername(StringEncryptUtils.encryptString(username));
        datingMapper.insertDatingProfile(datingProfile);
        return datingProfile.getProfileId();
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
    public DatingPick QueryDatingPick(Integer profileId, String username) throws WsgException {
        DatingPick datingPick = datingMapper.selectDatingPick(profileId
                , StringEncryptUtils.encryptString(username));
        if (datingPick == null) {
            return null;
        }
        datingPick.setUsername(StringEncryptUtils.decryptString(datingPick.getUsername()));
        return datingPick;
    }

    /**
     * 查找撩一下记录
     *
     * @param id
     * @return
     */
    public DatingPick QueryDatingPickById(Integer id) throws WsgException {
        DatingPick datingPick = datingMapper.selectDatingPickById(id);
        if (datingPick != null) {
            datingPick.setUsername(StringEncryptUtils.decryptString(datingPick.getUsername()));
            datingPick.getDatingProfile().setUsername(StringEncryptUtils.decryptString(datingPick.getDatingProfile().getUsername()));
            return datingPick;
        }
        return null;
    }

    /**
     * 添加撩一下记录
     *
     * @param datingPick
     * @return
     */
    public void AddDatingPick(String username, DatingPick datingPick) throws WsgException {
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
    public void UpdateDatingPickState(Integer id, Integer state) {
        datingMapper.updateDatingPickState(id, state);
        DatingPick datingPick = datingMapper.selectDatingPickById(id);
        DatingMessage datingMessage = new DatingMessage();
        datingMessage.setUsername(datingPick.getUsername());
        datingMessage.setType(1);
        datingMessage.setDatingPick(datingPick);
        datingMessage.setState(0);
        datingMapper.insertDatingMessage(datingMessage);
    }

    /**
     * 分页查找用户卖室友消息列表
     *
     * @param username
     * @param start
     * @param size
     * @return
     */
    public List<DatingMessage> QueryUserDatingMessage(String username, Integer start, Integer size) throws WsgException {
        List<DatingMessage> list = datingMapper.selectUserDatingMessagePage(StringEncryptUtils.encryptString(username), start, size);
        for (DatingMessage datingMessage : list) {
            datingMessage.setUsername(StringEncryptUtils.decryptString(datingMessage.getUsername()));
            datingMessage.getDatingPick().setUsername(StringEncryptUtils.decryptString(datingMessage.getDatingPick().getUsername()));
            datingMessage.getDatingPick().getDatingProfile().setUsername(StringEncryptUtils.decryptString(datingMessage.getDatingPick().getDatingProfile().getUsername()));
        }
        return list;
    }

    /**
     * 查询用户未读的通知消息数量
     *
     * @param username
     * @return
     */
    public Integer QueryUserUnReadDatingMessageCount(String username) throws WsgException {
        return datingMapper.selectUserUnReadDatingMessageCount(StringEncryptUtils.encryptString(username));
    }

    /**
     * 添加卖室友通知消息
     *
     * @param datingMessage
     * @return
     */
    public void AddDatingMessage(DatingMessage datingMessage) throws WsgException {
        datingMessage.setUsername(StringEncryptUtils.encryptString(datingMessage.getUsername()));
        datingMapper.insertDatingMessage(datingMessage);
    }

    /**
     * 更新卖室友通知消息阅读状态
     *
     * @param id
     * @param state
     * @return
     */
    public void UpdateDatingMessageState(Integer id, Integer state) {
        datingMapper.updateDatingMessageState(id, state);
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
