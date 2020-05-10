package edu.gdei.gdeiassistant.Service.LostAndFound;

import com.aliyun.oss.OSSClient;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class LostAndFoundService {

    @Resource(name = "lostAndFoundMapper")
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private UserProfileService userProfileService;

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
        String username = StringEncryptUtils.decryptString(lostAndFoundInfo.getLostAndFoundItem().getUsername());
        int itemId = lostAndFoundInfo.getLostAndFoundItem().getId();
        List<String> pictureURL = GetLostAndFoundItemPictureURL(itemId);
        lostAndFoundInfo.getLostAndFoundItem().setUsername(username);
        lostAndFoundInfo.getLostAndFoundItem().setPictureURL(pictureURL);
        //获取用户资料和头像信息
        lostAndFoundInfo.getProfile().setUsername(username);
        lostAndFoundInfo.getProfile().setAvatarURL(userProfileService.GetUserAvatar(username));
        return lostAndFoundInfo;
    }

    /**
     * 查询个人发布的失物招领物品信息
     *
     * @return
     */
    public List<LostAndFoundItem> QueryPersonalLostAndFoundItems(String username) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(StringEncryptUtils.encryptString(username));
        if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
            lostAndFoundItem.setUsername(username);
        }
        return lostAndFoundItemList;
    }

    /**
     * 添加失物招领物品信息
     *
     * @param lostAndFoundItem
     * @return
     */
    public LostAndFoundItem AddLostAndFoundItem(LostAndFoundItem lostAndFoundItem, String username) throws Exception {
        lostAndFoundItem.setUsername(StringEncryptUtils.encryptString(username));
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
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "lostandfound/" + id + "_" + index + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 获取失物招领物品图片
     *
     * @param id
     * @return
     */
    public List<String> GetLostAndFoundItemPictureURL(int id) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        List<String> pictureURL = new ArrayList<>();
        //检查图片是否存在
        for (int i = 1; i <= 4; i++) {
            if (ossClient.doesObjectExist("gdeiassistant-userdata", "lostandfound/" + id + "_" + i + ".jpg")) {
                //设置过期时间30分钟
                Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
                // 生成URL
                String url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "lostandfound/" + id + "_" + i + ".jpg", expiration).toString().replace("http", "https");
                pictureURL.add(url);
            } else {
                break;
            }
        }
        ossClient.shutdown();
        return pictureURL;
    }
}
