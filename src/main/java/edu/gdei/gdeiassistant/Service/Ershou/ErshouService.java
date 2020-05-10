package edu.gdei.gdeiassistant.Service.Ershou;

import com.aliyun.oss.OSSClient;
import edu.gdei.gdeiassistant.Exception.DatabaseException.ConfirmedStateException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.ErshouInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.ErshouItem;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou.ErshouMapper;
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
public class ErshouService {

    @Resource(name = "ershouMapper")
    private ErshouMapper ershouMapper;

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
        String username = StringEncryptUtils.decryptString(ershouInfo.getErshouItem().getUsername());
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
     * 查询个人发布的二手交易信息
     *
     * @return
     */
    public List<ErshouItem> QueryPersonalErShouItems(String username) throws Exception {
        List<ErshouItem> ershouItemList = ershouMapper.selectItemsByUsername(StringEncryptUtils.encryptString(username));
        if (ershouItemList == null || ershouItemList.isEmpty()) {
            return new ArrayList<>();
        }
        for (ErshouItem ershouItem : ershouItemList) {
            ershouItem.setUsername(username);
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
            String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
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
            String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
            ershouItem.setUsername(username);
        }
        return ershouItemList;
    }

    /**
     * 添加二手交易商品信息
     *
     * @param ershouItem
     * @return
     */
    public ErshouItem AddErshouItem(ErshouItem ershouItem, String username) throws Exception {
        //金额价格保留两位小数点
        ershouItem.setPrice((float) (Math.round(ershouItem.getPrice() * 100)) / 100);
        ershouItem.setUsername(StringEncryptUtils.encryptString(username));
        //使用24小时制显示发布时间
        ershouItem.setPublishTime(new Date());
        ershouMapper.insertItem(ershouItem);
        return ershouItem;
    }

    /**
     * 更新二手交易商品信息
     *
     * @param ershouItem
     * @param id
     * @return
     */
    public void UpdateErshouItem(ErshouItem ershouItem, int id) throws Exception {
        ershouItem.setId(id);
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (!ershouInfo.getErshouItem().getState().equals(2)) {
            ershouMapper.updateItem(ershouItem);
            return;
        }
        throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
    }

    /**
     * 更新二手交易信息状态
     *
     * @param id
     * @param state
     * @return
     */
    public void UpdateErshouItemState(int id, int state) throws Exception {
        ErshouInfo ershouInfo = ershouMapper.selectInfoByID(id);
        if (ershouInfo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (!ershouInfo.getErshouItem().getState().equals(2)) {
            ershouMapper.updateItemState(id, state);
            return;
        }
        throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
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
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        //上传文件
        ossClient.putObject("gdeiassistant-userdata", "ershou/" + id + "_" + index + ".jpg", inputStream);
        ossClient.shutdown();
    }

    /**
     * 获取二手交易商品图片
     *
     * @param id
     * @return
     */
    public List<String> GetErshouItemPictureURL(int id) {
        // 创建OSSClient实例
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
        List<String> pictureURL = new ArrayList<>();
        //检查二手交易商品图片是否存在
        for (int i = 1; i <= 4; i++) {
            if (ossClient.doesObjectExist("gdeiassistant-userdata", "ershou/" + id + "_" + i + ".jpg")) {
                //设置过期时间30分钟
                Date expiration = new Date(new Date().getTime() + 1000 * 60 * 30);
                // 生成URL
                String url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "ershou/" + id + "_" + i + ".jpg", expiration).toString().replace("http", "https");
                pictureURL.add(url);
            } else {
                break;
            }
        }
        ossClient.shutdown();
        return pictureURL;
    }
}
