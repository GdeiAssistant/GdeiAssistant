package cn.gdeiassistant.Service.Socialising.Dating;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.Exception.DatingException.RepeatPickException;
import cn.gdeiassistant.Exception.DatingException.SelfPickException;
import cn.gdeiassistant.Pojo.Entity.DatingMessage;
import cn.gdeiassistant.Pojo.Entity.DatingPick;
import cn.gdeiassistant.Pojo.Entity.DatingProfile;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Dating.DatingMapper;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.AliYunOSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class DatingService {

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private DatingMapper datingMapper;

    @Autowired
    private AliYunOSSUtils aliyunOssUtils;

    /**
     * 根据ID查找卖室友详细信息
     *
     * @param id
     * @return
     */
    public DatingProfile QueryDatingProfile(Integer id) throws DataNotExistException {
        DatingProfile datingProfile = datingMapper.selectDatingProfileById(id);
        if (datingProfile != null) {
            datingProfile.setUsername(datingProfile.getUsername());
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
    public List<DatingProfile> QueryDatingProfile(Integer start, Integer size, Integer area) {
        List<DatingProfile> list = datingMapper.selectDatingProfilePage(start, size, area);
        for (DatingProfile datingProfile : list) {
            datingProfile.setQq("请进入详情页查看");
            datingProfile.setWechat("请进入详情页查看");
            datingProfile.setUsername(datingProfile.getUsername());
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
     * @param sessionId
     * @param datingProfile
     * @return
     */
    public Integer AddDatingProfile(String sessionId, DatingProfile datingProfile) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        datingProfile.setUsername(user.getUsername());
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
        aliyunOssUtils.UploadOSSObject("gdeiassistant-userdata", "dating/" + id + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 查找撩一下记录
     *
     * @param profileId
     * @param sessionId
     * @return
     */
    public DatingPick QueryDatingPick(Integer profileId, String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        DatingPick datingPick = datingMapper.selectDatingPick(profileId, user.getUsername());
        if (datingPick == null) {
            return null;
        }
        datingPick.setUsername(datingPick.getUsername());
        return datingPick;
    }

    /**
     * 检查是否隐藏撩一下界面
     *
     * @param sessionId
     * @param pickId
     * @return
     */
    public boolean CheckIsPickPageHidden(String sessionId, int pickId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        DatingPick datingPick = QueryDatingPickById(pickId);
        if (datingPick != null) {
            if (datingPick.getState().equals(1)) {
                //如果对方已接受该撩一下请求，则隐藏撩一下界面，显示联系方式
                return true;
            }
            //当发布者与浏览者相同时，隐藏撩一下功能
            return datingPick.getUsername().equals(user.getUsername());
        }
        return false;
    }

    /**
     * 查找撩一下记录
     *
     * @param id
     * @return
     */
    public DatingPick QueryDatingPickById(int id) {
        DatingPick datingPick = datingMapper.selectDatingPickById(id);
        if (datingPick != null) {
            datingPick.setUsername(datingPick.getUsername());
            datingPick.getDatingProfile().setUsername(datingPick.getDatingProfile().getUsername());
            return datingPick;
        }
        return null;
    }

    public void VerifyDatingPickRequestAccess(String sessionId, int pickId) throws RepeatPickException, SelfPickException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        DatingPick datingPick = datingMapper.selectDatingPickById(pickId);
        if (datingPick != null) {
            //对方未拒绝前，不能发起多次撩一下请求
            if (!datingPick.getState().equals(-1)) {
                throw new RepeatPickException("重复的撩一下请求");
            }
            //不能向自己发布的卖室友信息发送撩一下请求
            if (datingPick.getDatingProfile().getUsername().equals(user.getUsername())) {
                throw new SelfPickException("不能向自己发布的卖室友信息发送撩一下请求");
            }
        }
    }

    /**
     * 检查当前用户有无查看撩一下记录的权限
     *
     * @param sessionId
     * @param id
     */
    public void VerifyDatingPickViewAccess(String sessionId, int id) throws DataNotExistException, NoAccessException {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        DatingPick datingPick = datingMapper.selectDatingPickById(id);
        if (datingPick != null) {
            if (datingPick.getDatingProfile().getUsername().equals(user.getUsername())) {
                return;
            }
            throw new NoAccessException("没有权限查看该撩一下信息");
        }
        throw new DataNotExistException("该撩一下信息不存在");
    }

    /**
     * 添加撩一下记录
     *
     * @param sessionId
     * @param datingPick
     * @return
     */
    public void AddDatingPick(String sessionId, DatingPick datingPick) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        datingPick.setUsername(user.getUsername());
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
    public void UpdateDatingPickState(Integer id, Integer state) throws DataNotExistException, NoAccessException {
        DatingPick datingPick = datingMapper.selectDatingPickById(id);
        if (datingPick != null) {
            if (datingPick.getState().equals(0)) {
                datingMapper.updateDatingPickState(id, state);
                DatingMessage datingMessage = new DatingMessage();
                datingMessage.setUsername(datingPick.getUsername());
                datingMessage.setType(1);
                datingMessage.setDatingPick(datingPick);
                datingMessage.setState(0);
                datingMapper.insertDatingMessage(datingMessage);
                return;
            }
            throw new NoAccessException("该撩一下记录已处理，请勿重复提交");
        }
        throw new DataNotExistException("该撩一下记录不存在");

    }

    /**
     * 分页查找用户卖室友消息列表
     *
     * @param sessionId
     * @param start
     * @param size
     * @return
     */
    public List<DatingMessage> QueryUserDatingMessage(String sessionId, Integer start, Integer size) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<DatingMessage> list = datingMapper.selectUserDatingMessagePage(user.getUsername(), start, size);
        for (DatingMessage datingMessage : list) {
            datingMessage.setUsername(datingMessage.getUsername());
            datingMessage.getDatingPick().setUsername(datingMessage.getDatingPick().getUsername());
            datingMessage.getDatingPick().getDatingProfile().setUsername(datingMessage.getDatingPick().getDatingProfile().getUsername());
        }
        return list;
    }

    /**
     * 查询用户未读的通知消息数量
     *
     * @param sessionId
     * @return
     */
    public Integer QueryUserUnReadDatingMessageCount(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return datingMapper.selectUserUnReadDatingMessageCount(user.getUsername());
    }

    /**
     * 添加卖室友通知消息
     *
     * @param datingMessage
     * @return
     */
    public void AddDatingMessage(DatingMessage datingMessage) {
        datingMessage.setUsername(datingMessage.getUsername());
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
        return aliyunOssUtils.GeneratePresignedUrl("gdeiassistant-userdata", "dating/" + id + ".jpg", 30, TimeUnit.MINUTES);
    }
}
