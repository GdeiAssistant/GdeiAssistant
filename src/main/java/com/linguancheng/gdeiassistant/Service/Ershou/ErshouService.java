package com.linguancheng.gdeiassistant.Service.Ershou;

import com.aliyun.oss.OSSClient;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Ershou.ErshouMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.AuthorProfile;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouItem;
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
public class ErshouService {

    @Autowired
    private UserProfileService userProfileService;

    @Resource(name = "ershouMapper")
    private ErshouMapper ershouMapper;

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

    private Log log = LogFactory.getLog(ErshouService.class);

    /**
     * 查询指定ID的二手交易商品信息
     *
     * @return
     */
    public BaseResult<ErshouItem, DataBaseResultEnum> QueryErshouItemByID(int id) {
        BaseResult<ErshouItem, DataBaseResultEnum> result = new BaseResult<>();
        try {
            ErshouItem ershouItem = ershouMapper.selectItemByID(id);
            if (ershouItem == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                //获取二手交易商品图片URL
                String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
                int itemId = ershouItem.getId();
                List<String> pictureURL = GetErshouItemPictureURL(itemId);
                ershouItem.setUsername(username);
                ershouItem.setPictureURL(pictureURL);
                result.setResultData(ershouItem);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("ID查询二手商品信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询指定ID的二手交易商品详细信息
     *
     * @param id
     * @return
     */
    public BaseResult<ErshouInfo, DataBaseResultEnum> QueryErshouItemDetailInfo(int id) {
        BaseResult<ErshouInfo, DataBaseResultEnum> result = new BaseResult<>();
        try {
            ErshouItem ershouItem = ershouMapper.selectItemByID(id);
            if (ershouItem == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                //获取二手交易商品图片URL
                String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
                int itemId = ershouItem.getId();
                List<String> pictureURL = GetErshouItemPictureURL(itemId);
                ershouItem.setPictureURL(pictureURL);
                //获取发布者用户个人资料
                BaseResult<AuthorProfile, DataBaseResultEnum> queryProfileResult = userProfileService.GetUserAuthorProfile(username);
                switch (queryProfileResult.getResultType()) {
                    case SUCCESS:
                        AuthorProfile authorProfile = queryProfileResult.getResultData();
                        ErshouInfo ershouInfo = new ErshouInfo();
                        ershouInfo.setErshouItem(ershouItem);
                        ershouInfo.setAuthorProfile(authorProfile);
                        result.setResultType(DataBaseResultEnum.SUCCESS);
                        result.setResultData(ershouInfo);
                        break;

                    default:
                        result.setResultType(DataBaseResultEnum.ERROR);
                        break;
                }
            }
        } catch (Exception e) {
            log.error("ID查询二手商品详细信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询个人发布的二手交易信息
     *
     * @return
     */
    public BaseResult<List<ErshouItem>, DataBaseResultEnum> QueryPersonalErShouItem(String username) {
        BaseResult<List<ErshouItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<ErshouItem> ershouItemList = ershouMapper.selectItemByUsername(StringEncryptUtils.encryptString(username));
            if (ershouItemList == null || ershouItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (ErshouItem ershouItem : ershouItemList) {
                    ershouItem.setUsername(username);
                }
                result.setResultData(ershouItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("查询个人二手商品信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 分页查询二手交易信息
     *
     * @param start
     * @return
     */
    public BaseResult<List<ErshouItem>, DataBaseResultEnum> QueryErshouItem(int start) {
        BaseResult<List<ErshouItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<ErshouItem> ershouItemList = ershouMapper.selectAvailableItem(start, 10);
            if (ershouItemList == null || ershouItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (ErshouItem ershouItem : ershouItemList) {
                    String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
                    ershouItem.setUsername(username);
                }
                result.setResultData(ershouItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("分页查询二手商品信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询关键词的二手交易信息
     *
     * @param keyword
     * @param start
     * @return
     */
    public BaseResult<List<ErshouItem>, DataBaseResultEnum> QueryErshouItemWithKeyword(String keyword, int start) {
        BaseResult<List<ErshouItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<ErshouItem> ershouItemList = ershouMapper.selectItemWithKeyword(start, 10, keyword);
            if (ershouItemList == null || ershouItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (ErshouItem ershouItem : ershouItemList) {
                    String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
                    ershouItem.setUsername(username);
                }
                result.setResultData(ershouItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("关键词查询二手商品信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 查询指定类型的二手交易信息
     *
     * @param type
     * @param start
     * @return
     */
    public BaseResult<List<ErshouItem>, DataBaseResultEnum> QueryErshouItemByType(int type, int start) {
        BaseResult<List<ErshouItem>, DataBaseResultEnum> result = new BaseResult<>();
        try {
            List<ErshouItem> ershouItemList = ershouMapper.selectItemByType(start, 10, type);
            if (ershouItemList == null || ershouItemList.isEmpty()) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                for (ErshouItem ershouItem : ershouItemList) {
                    String username = StringEncryptUtils.decryptString(ershouItem.getUsername());
                    ershouItem.setUsername(username);
                }
                result.setResultData(ershouItemList);
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            log.error("指定类型查询二手商品信息异常：" , e);
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 添加二手交易商品信息
     *
     * @param ershouItem
     * @return
     */
    public BaseResult<ErshouItem, BoolResultEnum> AddErshouInfo(ErshouItem ershouItem, String username) {
        BaseResult<ErshouItem, BoolResultEnum> result = new BaseResult<>();
        try {
            //金额价格保留两位小数点
            ershouItem.setPrice((float) (Math.round(ershouItem.getPrice() * 100)) / 100);
            ershouItem.setUsername(StringEncryptUtils.encryptString(username));
            //使用24小时制显示发布时间
            ershouItem.setPublishTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
            ershouMapper.insertItem(ershouItem);
            result.setResultData(ershouItem);
            result.setResultType(BoolResultEnum.SUCCESS);
        } catch (Exception e) {
            log.error("保存二手商品信息异常：" , e);
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 更新二手交易商品信息
     *
     * @param ershouItem
     * @param id
     * @return
     */
    public BoolResultEnum UpdateErshouInfo(ErshouItem ershouItem, int id) {
        try {
            ershouItem.setId(id);
            ershouMapper.updateItemInfo(ershouItem);
            return BoolResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新二手商品信息异常：" , e);
            return BoolResultEnum.ERROR;
        }
    }

    /**
     * 更新二手交易信息状态
     *
     * @param id
     * @param state
     * @return
     */
    public BoolResultEnum UpdateErshouItemState(int id, int state) {
        try {
            ershouMapper.updateItemState(id, state);
            return BoolResultEnum.SUCCESS;
        } catch (Exception e) {
            log.error("更新二手商品信息状态异常：" , e);
            return BoolResultEnum.ERROR;
        }
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
