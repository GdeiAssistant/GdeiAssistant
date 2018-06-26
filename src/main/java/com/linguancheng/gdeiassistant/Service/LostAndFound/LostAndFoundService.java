package com.linguancheng.gdeiassistant.Service.LostAndFound;

import com.aliyun.oss.OSSClient;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.AuthorProfile;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.Profile.UserProfileService;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.InputStream;
import java.text.SimpleDateFormat;
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

    private Log log = LogFactory.getLog(LostAndFoundService.class);

    /**
     * 查询指定ID的失物招领物品信息
     *
     * @param id
     * @return
     */
    public BaseResult<LostAndFoundItem, DataBaseResultEnum> QueryLostAndFoundItemByID(int id) {
        BaseResult<LostAndFoundItem, DataBaseResultEnum> result = new BaseResult<>();
        try {
            LostAndFoundItem lostAndFoundItem = lostAndFoundMapper.selectItemByID(id);
            if (lostAndFoundItem == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                //获取二手交易商品图片URL
                String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                int itemId = lostAndFoundItem.getId();
                List<String> pictureURL = GetLostAndFoundItemPictureURL(username, itemId);
                lostAndFoundItem.setUsername(username);
                lostAndFoundItem.setPictureURL(pictureURL);
                result.setResultData(lostAndFoundItem);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("ID查询失物招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询指定ID的失物招领物品详细信息
     *
     * @return
     */
    public BaseResult<LostAndFoundInfo, DataBaseResultEnum> QueryLostAndFoundInfoByID(int id) {
        BaseResult<LostAndFoundInfo, DataBaseResultEnum> result = new BaseResult<>();
        try {
            LostAndFoundItem lostAndFoundItem = lostAndFoundMapper.selectItemByID(id);
            if (lostAndFoundItem == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                //获取二手交易商品图片URL
                String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                int itemId = lostAndFoundItem.getId();
                List<String> pictureURL = GetLostAndFoundItemPictureURL(username, itemId);
                lostAndFoundItem.setUsername(username);
                lostAndFoundItem.setPictureURL(pictureURL);
                //获取发布者用户个人资料
                BaseResult<AuthorProfile, DataBaseResultEnum> queryProfileResult = userProfileService
                        .GetUserAuthorProfile(username);
                switch (queryProfileResult.getResultType()) {
                    case SUCCESS:
                        AuthorProfile authorProfile = queryProfileResult.getResultData();
                        LostAndFoundInfo lostAndFoundInfo = new LostAndFoundInfo();
                        lostAndFoundInfo.setLostAndFoundItem(lostAndFoundItem);
                        lostAndFoundInfo.setAuthorProfile(authorProfile);
                        result.setResultType(DataBaseResultEnum.SUCCESS);
                        result.setResultData(lostAndFoundInfo);
                        break;

                    default:
                        result.setResultType(DataBaseResultEnum.ERROR);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("ID查询失物招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询个人发布的失物招领物品信息
     *
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryPersonalLostAndFoundItem(String username) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                    .selectItemByUsername(StringEncryptUtils.encryptString(username));
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("查询个人失物招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 分页查询失物信息
     *
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryLostItem(int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper.selectAvailableItem(0, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("分页查询失物物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 分页查询招领信息
     *
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryFoundItem(int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper.selectAvailableItem(1, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("分页查询招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询关键词的失物信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryLostItemWithKeyword(String keyword, int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                    .selectItemWithKeyword(0, keyword, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("关键词查询失物物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询关键词的招领信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryFoundItemWithKeyword(String keyword, int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                    .selectItemWithKeyword(1, keyword, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("关键词查询招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询指定类型的失物信息
     *
     * @param type
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryLostItemByType(int type, int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                    .selectItemByItemType(1, type, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("指定类型查询失物物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询指定类型的招领信息
     *
     * @param type
     * @param start
     * @return
     */
    public BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> QueryFoundItemByType(int type, int start) {
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                    .selectItemByItemType(1, type, start, 10);
            if (lostAndFoundItemList == null || lostAndFoundItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    String username = StringEncryptUtils.decryptString(lostAndFoundItem.getUsername());
                    lostAndFoundItem.setUsername(username);
                }
                result.setResultData(lostAndFoundItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("指定类型查询招领物品异常：" + e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 添加失物招领物品信息
     *
     * @param lostAndFoundItem
     * @return
     */
    public BaseResult<LostAndFoundItem, BoolResultEnum> AddLostAndFoundItem(LostAndFoundItem lostAndFoundItem, String username) {
        BaseResult<LostAndFoundItem, BoolResultEnum> result = new BaseResult<>();
        try {
            lostAndFoundItem.setUsername(StringEncryptUtils.encryptString(username));
            //使用24小时制显示发布时间
            lostAndFoundItem.setPublishTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            lostAndFoundMapper.insertItem(lostAndFoundItem);
            result.setResultData(lostAndFoundItem);
            result.setResultType(BoolResultEnum.SUCCESS);
        } catch (Exception e) {
            log.error("保存失物招领物品异常：" + e);
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 更新失物招领商品信息
     *
     * @param lostAndFoundItem
     * @param id
     * @return
     */
    public BoolResultEnum UpdateLostAndFoundItem(LostAndFoundItem lostAndFoundItem, int id) {
        try {
            lostAndFoundItem.setId(id);
            lostAndFoundMapper.updateItemInfo(lostAndFoundItem);
            return BoolResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新失物招领物品异常：" + e);
            return BoolResultEnum.ERROR;
        }
    }

    /**
     * 更新失物招领物品状态
     *
     * @param id
     * @param state
     * @return
     */
    public BoolResultEnum UpdateLostAndFoundItemState(int id, int state) {
        try {
            lostAndFoundMapper.updateItemState(id, state);
            return BoolResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新失物招领物品状态异常：" + e);
            return BoolResultEnum.ERROR;
        }
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
     * @param username
     * @param id
     * @return
     */
    public List<String> GetLostAndFoundItemPictureURL(String username, int id) {
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
