package cn.gdeiassistant.Service.Socialising.Ershou;

import cn.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.Pojo.Entity.ErshouInfo;
import cn.gdeiassistant.Pojo.Entity.ErshouItem;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou.ErshouMapper;
import cn.gdeiassistant.Service.AccountManagement.Profile.UserProfileService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.AliyunOSSUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ErshouService {

    @Resource(name = "ershouMapper")
    private ErshouMapper ershouMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AliyunOSSUtils aliyunOssUtils;

    /**
     * 查询指定ID的二手交易商品信息
     *
     * @return
     */
    public ErshouInfo QueryErshouInfoByID(int id) throws Exception {
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo == null) {
            throw new DataNotExistException("二手交易商品不存在");
        }
        //获取二手交易商品图片URL
        String username = ershouInfo.getErshouItem().getUsername();
        int itemId = ershouInfo.getErshouItem().getId();
        List<String> pictureURL = GetErshouItemPictureURL(itemId);
        ershouInfo.getErshouItem().setUsername(username);
        ershouInfo.getErshouItem().setPictureURL(pictureURL);
        //获取用户资料和头像信息
        ershouInfo.getProfile().setUsername(username);
        ershouInfo.getProfile().setAvatarURL(userProfileService.GetUserAvatar(username));
        return ershouInfo;
    }

    /**
     * 校验当前用户有无编辑权限
     *
     * @param sessionId
     * @param id
     */
    public void VerifyErshouInfoEditAccess(String sessionId, int id) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo != null) {
            if (ershouInfo.getErshouItem().getUsername().equals(user.getUsername())) {
                if (!ershouInfo.getErshouItem().getState().equals(2)) {
                    //校验通过
                    return;
                }
                throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
            }
            throw new NoAccessException("没有权限编辑该二手交易信息");
        }
        throw new DataNotExistException("二手交易商品不存在");
    }

    /**
     * 查询个人发布的二手交易信息
     *
     * @param sessionId
     * @return
     */
    public List<ErshouItem> QueryPersonalErShouItems(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<ErshouItem> ershouItemList = ershouMapper.selectItemsByUsername(user.getUsername());
        if (ershouItemList == null || ershouItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (ErshouItem ershouItem : ershouItemList) {
            ershouItem.setUsername(user.getUsername());
        }
        return ershouItemList;
    }

    /**
     * 分页查询二手交易信息
     *
     * @param start
     * @return
     */
    public List<ErshouItem> QueryErshouItems(int start) throws Exception {
        List<ErshouItem> ershouItemList = ershouMapper.selectAvailableItems(start, 10);
        if (ershouItemList == null || ershouItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (ErshouItem ershouItem : ershouItemList) {
            String username = ershouItem.getUsername();
            ershouItem.setUsername(username);
        }
        return ershouItemList;
    }

    /**
     * 查询关键词的二手交易信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public List<ErshouItem> QueryErshouItemsWithKeyword(String keyword, int start) throws Exception {
        List<ErshouItem> ershouItemList = ershouMapper.selectItemsWithKeyword(start, 10, keyword);
        if (ershouItemList == null || ershouItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (ErshouItem ershouItem : ershouItemList) {
            String username =ershouItem.getUsername();
            ershouItem.setUsername(username);
        }
        return ershouItemList;
    }

    /**
     * 查询指定类型的二手交易信息
     *
     * @param type
     * @param start
     * @return
     */
    public List<ErshouItem> QueryErshouItemByType(int type, int start) throws Exception {
        List<ErshouItem> ershouItemList = ershouMapper.selectItemsByType(start, 10, type);
        if (ershouItemList == null || ershouItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (ErshouItem ershouItem : ershouItemList) {
            String username =ershouItem.getUsername();
            ershouItem.setUsername(username);
        }
        return ershouItemList;
    }

    /**
     * 添加二手交易商品信息
     *
     * @param ershouItem
     * @param sessionId
     * @return
     */
    public ErshouItem AddErshouItem(ErshouItem ershouItem, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        //金额价格保留两位小数点
        ershouItem.setPrice((float) (Math.round(ershouItem.getPrice() * 100)) / 100);
        ershouItem.setUsername(user.getUsername());
        //使用24小时制显示发布时间
        ershouItem.setPublishTime(new Date());
        ershouMapper.insertItem(ershouItem);
        return ershouItem;
    }

    /**
     * 更新二手交易商品信息
     *
     * @param sessionId
     * @param ershouItem
     * @param id
     * @return
     */
    public void UpdateErshouItem(String sessionId, ErshouItem ershouItem, int id) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ershouItem.setId(id);
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (ershouInfo.getErshouItem().getUsername().equals(user.getUsername())) {
            if (!ershouInfo.getErshouItem().getState().equals(2)) {
                ershouMapper.updateItem(ershouItem);
                return;
            }
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限修改该商品信息");

    }

    /**
     * 更新二手交易信息状态
     *
     * @param id
     * @param state
     * @return
     */
    public void UpdateErshouItemState(String sessionId, int id, int state) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (ershouInfo.getErshouItem().getUsername().equals(user.getUsername())) {
            if (!ershouInfo.getErshouItem().getState().equals(2)) {
                ershouMapper.updateItemState(id, state);
            }
            //已出售状态的商品不能修改状态
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限修改该商品信息");
    }

    /**
     * 上传二手交易商品图片
     *
     * @param id
     * @param index
     * @param inputStream
     * @return
     */
    public void UploadErshouItemPicture(int id, int index, InputStream inputStream) {
        aliyunOssUtils.UploadOSSObject("gdeiassistant-userdata", "ershou/" + id + "_" + index + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取二手交易商品图片
     *
     * @param id
     * @return
     */
    public List<String> GetErshouItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = aliyunOssUtils.GeneratePresignedUrl("gdeiassistant-userdata", "ershou/" + id + "_" + i + ".jpg"
                    , 30, TimeUnit.MINUTES);
            if (StringUtils.isNotBlank(url)) {
                pictureURL.add(url);
            } else {
                break;
            }
        }
        return pictureURL;
    }
}
