package cn.gdeiassistant.Service.Socialising.LostAndFound;

import cn.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import cn.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import cn.gdeiassistant.Pojo.Entity.User;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import cn.gdeiassistant.Service.AccountManagement.Profile.UserProfileService;
import cn.gdeiassistant.Service.AccountManagement.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.AliYunOSSUtils;
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
public class LostAndFoundService {

    @Resource(name = "lostAndFoundMapper")
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private AliYunOSSUtils aliyunOssUtils;

    /**
     * 查询指定ID的失物招领物品信息
     *
     * @param id
     * @return
     */
    public LostAndFoundInfo QueryLostAndFoundInfoByID(int id) throws Exception {
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundMapper.selectInfoByID(id);
        if (lostAndFoundInfo == null) {
            throw new DataNotExistException("失物招领信息不存在");
        }
        if (lostAndFoundInfo.getLostAndFoundItem().getState().equals(1)) {
            throw new ConfirmedStateException("物品已确认寻回，不可再次编辑和查看");
        }
        //获取二手交易商品图片URL
        String username = lostAndFoundInfo.getLostAndFoundItem().getUsername();
        int itemId = lostAndFoundInfo.getLostAndFoundItem().getId();
        List<String> pictureURL = GetLostAndFoundItemPictureURL(itemId);
        lostAndFoundInfo.getLostAndFoundItem().setUsername(username);
        lostAndFoundInfo.getLostAndFoundItem().setPictureURL(pictureURL);
        //获取用户资料和头像信息
        lostAndFoundInfo.getProfile().setUsername(username);
        lostAndFoundInfo.getProfile().setAvatarURL(userProfileService.GetSelfUserAvatar(username));
        return lostAndFoundInfo;
    }

    /**
     * 查询个人发布的失物招领物品信息
     *
     * @param sessionId
     * @return
     */
    public List<LostAndFoundItem> QueryPersonalLostAndFoundItems(String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(user.getUsername());
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            lostAndFoundItem.setUsername(user.getUsername());
        }
        return lostAndFoundItemList;
    }

    /**
     * 检查当前用户有无编辑权限
     *
     * @param sessionId
     * @param id
     */
    public void VerifyLostAndFoundInfoEditAccess(String sessionId, int id) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundMapper.selectInfoByID(id);
        if (lostAndFoundInfo != null) {
            if (lostAndFoundInfo.getLostAndFoundItem().getUsername().equals(user.getUsername())) {
                return;
            }
            throw new NoAccessException("没有权限编辑该失物招领信息");
        }
        throw new DataNotExistException("失物招领信息不存在");
    }

    /**
     * 分页查询失物信息
     *
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryLostItems(int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper.selectAvailableItem(0, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 分页查询招领信息
     *
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryFoundItems(int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper.selectAvailableItem(1, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 查询关键词的失物信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryLostItemsWithKeyword(String keyword, int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemWithKeyword(0, keyword, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 查询关键词的招领信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryFoundItemsWithKeyword(String keyword, int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemWithKeyword(1, keyword, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 查询指定类型的失物信息
     *
     * @param type
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryLostItemsByType(int type, int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByItemType(1, type, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 查询指定类型的招领信息
     *
     * @param type
     * @param start
     * @return
     */
    public List<LostAndFoundItem> QueryFoundItemsByType(int type, int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByItemType(1, type, start, 10);
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            String username = lostAndFoundItem.getUsername();
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 添加失物招领物品信息
     *
     * @param lostAndFoundItem
     * @param sessionId
     * @return
     */
    public LostAndFoundItem AddLostAndFoundItem(LostAndFoundItem lostAndFoundItem, String sessionId) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        lostAndFoundItem.setUsername(user.getUsername());
        //使用24小时制显示发布时间
        lostAndFoundItem.setPublishTime(new Date());
        lostAndFoundMapper.insertItem(lostAndFoundItem);
        return lostAndFoundItem;
    }

    /**
     * 更新失物招领商品信息
     *
     * @param lostAndFoundItem
     * @param id
     * @return
     */
    public void UpdateLostAndFoundItem(LostAndFoundItem lostAndFoundItem, int id) throws Exception {
        lostAndFoundItem.setId(id);
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundMapper.selectInfoByID(id);
        if (lostAndFoundInfo == null) {
            throw new DataNotExistException("查找的失物招领信息不存在");
        }
        if (!lostAndFoundInfo.getLostAndFoundItem().getState().equals(1)) {
            lostAndFoundMapper.updateItemItem(lostAndFoundItem);
            return;
        }
        throw new ConfirmedStateException("物品已确认寻回，不可再次编辑");
    }

    /**
     * 更新失物招领物品状态
     *
     * @param id
     * @param state
     * @return
     */
    public void UpdateLostAndFoundItemState(int id, int state) throws Exception {
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundMapper.selectInfoByID(id);
        if (lostAndFoundInfo == null) {
            throw new DataNotExistException("查找的失物招领信息不存在");
        }
        if (!lostAndFoundInfo.getLostAndFoundItem().getState().equals(1)) {
            lostAndFoundMapper.updateItemState(id, state);
            return;
        }
        throw new ConfirmedStateException("物品已确认寻回，不可再次编辑");
    }

    /**
     * 上传失物招领物品图片
     *
     * @param id
     * @param index
     * @param inputStream
     * @return
     */
    public void UploadLostAndFoundItemPicture(int id, int index, InputStream inputStream) {
        aliyunOssUtils.UploadOSSObject("gdeiassistant-userdata", "lostandfound/" + id + "_" + index + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取失物招领物品图片
     *
     * @param id
     * @return
     */
    public List<String> GetLostAndFoundItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = aliyunOssUtils.GeneratePresignedUrl("gdeiassistant-userdata", "lostandfound/" + id + "_" + i + ".jpg"
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
