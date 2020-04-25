package edu.gdei.gdeiassistant.Service.UserData;

import com.alibaba.fastjson.JSON;
import com.aliyun.oss.OSSClient;
import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Constant.ItemConstantUtils;
import edu.gdei.gdeiassistant.Pojo.Entity.*;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Data.AppDataMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Phone.PhoneMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Privacy.PrivacyMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Profile.ProfileMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.User.UserMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantLogs.Mapper.Data.LogDataMapper;
import edu.gdei.gdeiassistant.Repository.Redis.ExportData.ExportDataDao;
import edu.gdei.gdeiassistant.Service.Profile.UserProfileService;
import edu.gdei.gdeiassistant.Tools.LocationUtils;
import edu.gdei.gdeiassistant.Tools.ReflectionUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class UserDataService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Resource(name = "phoneMapper")
    private PhoneMapper phoneMapper;

    @Resource(name = "profileMapper")
    private ProfileMapper profileMapper;

    @Resource(name = "privacyMapper")
    private PrivacyMapper privacyMapper;

    @Resource(name = "appDataMapper")
    private AppDataMapper appDataMapper;

    @Resource(name = "logDataMapper")
    private LogDataMapper logDataMapper;

    @Autowired
    private ExportDataDao exportDataDao;

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
     * 检查24小时内有无导出用户数据的记录
     *
     * @param username
     * @return
     */
    public boolean CheckAlreadyExportUserData(String username) {
        return StringUtils.isNotBlank(exportDataDao.QueryExportDataToken(username));
    }

    /**
     * 检查当前用户是否已有导出数据任务
     *
     * @param username
     * @return
     */
    public boolean CheckExportingUserData(String username) {
        return StringUtils.isNotBlank(exportDataDao.QueryExportingDataToken(username));
    }

    /**
     * 导出用户数据
     *
     * @param username
     * @return
     */
    @SuppressWarnings("unchecked")
    @Async
    public void ExportUserData(String username) throws WsgException, IOException {

        //写入导出任务记录
        exportDataDao.SaveExportingDataToken(username, UUID.randomUUID().toString());

        ByteArrayOutputStream byteArrayOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        Map<String, InputStream> userDataInputStreamMap = new HashMap<>();
        OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("username", username);
            //下载用户头像
            if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + ".jpg")) {
                InputStream avatar = ossClient.getObject("gdeiassistant-userdata", "avatar/" + username + ".jpg").getObjectContent();
                userDataInputStreamMap.put("avatar.jpg", avatar);
            }
            //下载用户高清头像
            if (ossClient.doesObjectExist("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg")) {
                InputStream avatar = ossClient.getObject("gdeiassistant-userdata", "avatar/" + username + "_hd.jpg").getObjectContent();
                userDataInputStreamMap.put("avatar_hd.jpg", avatar);
            }
            //获取绑定手机信息
            Phone phone = phoneMapper.selectPhone(StringEncryptUtils.encryptString(username));
            if (phone != null) {
                phone.setPhone(StringEncryptUtils.decryptString(phone.getPhone()));
                //隐藏用户绑定的手机号
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < phone.getPhone().length(); i++) {
                    if (i < 3) {
                        stringBuilder.append(phone.getPhone().charAt(i));
                    } else {
                        stringBuilder.append('*');
                    }
                }
                phone.setPhone(stringBuilder.toString());
                data.put("phone", phone);
            }
            //获取个人资料信息
            Profile profile = appDataMapper.selectUserProfile(StringEncryptUtils.encryptString(username));
            if (profile != null) {
                data.put("profile", profile);
            }
            //获取个人简介信息
            Introduction introduction = appDataMapper.selectUserIntroduction(StringEncryptUtils.encryptString(username));
            if (introduction != null && introduction.getIntroductionContent() != null) {
                data.put("introduction", introduction.getIntroductionContent());
            }
            //获取用户隐私设置
            Privacy privacy = appDataMapper.selectUserPrivacy(StringEncryptUtils.encryptString(username));
            if (privacy != null) {
                data.put("privacy", privacy);
            }
            //获取毕业账号处理方案设置
            Graduation graduation = appDataMapper.selectUserGraduation(StringEncryptUtils.encryptString(username));
            if (graduation != null) {
                data.put("graduation", graduation.getProgram());
            }
            //获取保存的四六级准考证号
            CetNumber cetNumber = appDataMapper.selectUserCetNumber(StringEncryptUtils.encryptString(username));
            if (cetNumber != null && cetNumber.getNumber() != null) {
                data.put("cet", cetNumber);
            }
            //获取实名认证信息
            Authentication authentication = appDataMapper.selectUserAuthentication(StringEncryptUtils.encryptString(username));
            if (authentication != null) {
                data.put("authentication", authentication);
            }
            //获取全民快递订单信息
            List<DeliveryOrder> deliveryOrderList = appDataMapper.selectUserDeliveryOrderList(StringEncryptUtils.encryptString(username));
            if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
                data.put("deliveryOrders", deliveryOrderList);
            }
            //获取全名快递交易信息
            List<DeliveryTrade> deliveryTradeList = appDataMapper.selectUserDeliveryTradeList(StringEncryptUtils.encryptString(username));
            if (deliveryTradeList != null && !deliveryTradeList.isEmpty()) {
                data.put("deliveryTrades", deliveryTradeList);
            }
            //获取二手交易信息
            List<ErshouItem> ershouItemList = appDataMapper.selectUserErshouItemList(StringEncryptUtils.encryptString(username));
            if (ershouItemList != null && !ershouItemList.isEmpty()) {
                for (ErshouItem ershouItem : ershouItemList) {
                    //下载二手交易图片
                    for (int i = 0; i <= 3; i++) {
                        if (ossClient.doesObjectExist("gdeiassistant-userdata", "ershou/" + ershouItem.getId() + "_" + i + ".jpg")) {
                            InputStream image = ossClient.getObject("gdeiassistant-userdata", "ershou/" + ershouItem.getId() + "_" + i + ".jpg").getObjectContent();
                            userDataInputStreamMap.put("ershou_" + ershouItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("ershouItems", ershouItemList);
            }
            //获取失物招领信息
            List<LostAndFoundItem> lostAndFoundItemList = appDataMapper.selectUserLostAndFoundItemList(StringEncryptUtils.encryptString(username));
            if (lostAndFoundItemList != null && !lostAndFoundItemList.isEmpty()) {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    //下载失物招领图片
                    for (int i = 0; i <= 3; i++) {
                        if (ossClient.doesObjectExist("gdeiassistant-userdata", "lostandfound/" + lostAndFoundItem.getId() + "_" + i + ".jpg")) {
                            InputStream image = ossClient.getObject("gdeiassistant-userdata", "lostandfound/" + lostAndFoundItem.getId() + "_" + i + ".jpg").getObjectContent();
                            userDataInputStreamMap.put("lostandfound_" + lostAndFoundItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("lostandfoundItems", lostAndFoundItemList);
            }
            //获取校园树洞信息
            List<Secret> secretList = appDataMapper.selectUserSecretItemList(StringEncryptUtils.encryptString(username));
            if (secretList != null && !secretList.isEmpty()) {
                for (Secret secret : secretList) {
                    if (secret.getSecretCommentList() != null && !secret.getSecretCommentList().isEmpty()) {
                        for (SecretComment secretComment : secret.getSecretCommentList()) {
                            //隐藏树洞评论信息发布者的用户名
                            secretComment.setUsername(null);
                        }
                        //设置评论数量
                        secret.setCommentCount(secret.getSecretCommentList().size());
                    } else {
                        //设置评论数量
                        secret.setCommentCount(0);
                    }
                    if (secret.getType().equals(1)) {
                        //下载语音信息
                        if (ossClient.doesObjectExist("gdeiassistant-userdata", "secret/voice/" + secret.getId() + ".mp3")) {
                            InputStream voice = ossClient.getObject("gdeiassistant-userdata", "secret/voice/" + secret.getId() + ".mp3").getObjectContent();
                            userDataInputStreamMap.put("secret_voice_" + secret.getId() + ".mp3", voice);
                        } else {
                            break;
                        }
                    }
                }
                data.put("secretItems", secretList);
            }
            //获取拍好校园信息
            List<Photograph> photographList = appDataMapper.selectUserPhotographItemList(StringEncryptUtils.encryptString(username));
            if (photographList != null && !photographList.isEmpty()) {
                for (Photograph photograph : photographList) {
                    //下载拍好校园图片
                    for (int i = 1; i <= photograph.getCount(); i++) {
                        if (ossClient.doesObjectExist("gdeiassistant-userdata", "photograph/" + photograph.getId() + "_" + i + ".jpg")) {
                            InputStream image = ossClient.getObject("gdeiassistant-userdata", "photograph/" + photograph.getId() + "_" + i + ".jpg").getObjectContent();
                            userDataInputStreamMap.put("photograph_" + photograph.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("photographItems", photographList);
            }
            //获取表白墙信息
            List<Express> expressList = appDataMapper.selectUserExpresssItemList(StringEncryptUtils.encryptString(username));
            if (expressList != null && !expressList.isEmpty()) {
                data.put("expressItems", expressList);
            }
            //获取校园卡充值日志记录
            List<ChargeLog> chargeLogList = logDataMapper.selectChargeLogList(StringEncryptUtils.encryptString(username));
            if (chargeLogList != null && !chargeLogList.isEmpty()) {
                data.put("chargeLogs", chargeLogList);
            }
            //移除Map中属性值为空的属性
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                if (Entity.class.isAssignableFrom(entry.getValue().getClass())) {
                    Map<String, Object> temp = ReflectionUtils.getAllNotNullObjectFields(entry.getValue(), entry.getValue().getClass());
                    map.put(entry.getKey(), temp);
                } else if (List.class.isAssignableFrom(entry.getValue().getClass())) {
                    List<Map<String, Object>> list = new ArrayList<>();
                    for (Object object : (List) entry.getValue()) {
                        Map<String, Object> temp = ReflectionUtils.getAllNotNullObjectFields(object, object.getClass());
                        list.add(temp);
                    }
                    map.put(entry.getKey(), list);
                } else {
                    map.put(entry.getKey(), entry.getValue());
                }
            }
            //使用字符串描述值替换部分参数值
            if (map.containsKey("deliveryOrders")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("deliveryOrders")) {
                    object.put("state", ItemConstantUtils.DELIVERY_ORDER_STATE_TYPE[(int) object.get("state")]);
                }
            }
            if (map.containsKey("deliveryTrades")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("deliveryTrades")) {
                    object.put("state", ItemConstantUtils.DELIVERY_TRADE_STATE_TYPE[(int) object.get("state")]);
                }
            }
            if (map.containsKey("ershouItems")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("ershouItems")) {
                    object.put("type", ItemConstantUtils.ERSHOU_ITEM_TYPE[(int) object.get("type")]);
                    object.put("state", ItemConstantUtils.ERSHOU_STATE_TYPE[(int) object.get("state")]);
                }
            }
            if (map.containsKey("lostandfoundItems")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("lostandfoundItems")) {
                    object.put("itemType", ItemConstantUtils.LOST_AND_FOUND_ITEM_TYPE[(int) object.get("itemType")]);
                    object.put("lostType", ItemConstantUtils.LOST_AND_FOUND_LOST_TYPE[(int) object.get("lostType")]);
                    object.put("state", ItemConstantUtils.LOST_AND_FOUND_STATE_TYPE[(int) object.get("state")]);
                }
            }
            if (map.containsKey("secretItems")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("secretItems")) {
                    object.put("state", ItemConstantUtils.SECRET_STATE_TYPE[(int) object.get("state")]);
                    object.put("type", ItemConstantUtils.SECRET_ITEM_TYPE[(int) object.get("type")]);
                }
            }
            if (map.containsKey("photographItems")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("photographItems")) {
                    object.put("type", ItemConstantUtils.PHOTOGRAPH_ITEM_TYPE[(int) object.get("type")]);
                }
            }
            if (map.containsKey("expressItems")) {
                for (Map<String, Object> object : (List<Map<String, Object>>) map.get("expressItems")) {
                    object.put("selfGender", ItemConstantUtils.EXPRESS_ITEM_GENDER_TYPE[(int) object.get("type")]);
                    object.put("personGender", ItemConstantUtils.EXPRESS_ITEM_GENDER_TYPE[(int) object.get("type")]);
                }
            }
            if (map.containsKey("profile")) {
                Map<String, Object> object = (Map<String, Object>) map.get("profile");
                if (object.containsKey("degree")) {
                    String degree = (String) UserProfileService.getDegreeMap().get(object.get("degree"));
                    object.put("degree", degree);
                }
                if (object.containsKey("gender")) {
                    if (object.get("gender").equals(3)) {
                        String gender = (String) object.get("customGenderName");
                        object.put("gender", gender);
                        object.remove("customGenderName");
                    } else {
                        String gender = (String) UserProfileService.getGenderMap().get(object.get("gender"));
                        object.put("gender", gender);
                    }
                }
                if (object.containsKey("profession")) {
                    String profession = (String) UserProfileService.getProfessionMap().get(object.get("profession"));
                    object.put("profession", profession);
                }
                if (object.containsKey("faculty")) {
                    String faculty = (String) UserProfileService.getFacultyMap().get(object.get("faculty"));
                    object.put("faculty", faculty);
                }
                if (object.containsKey("locationRegion")) {
                    Map<String, Region> regionMap = LocationUtils.getRegionMap();
                    String regionCode = (String) object.get("locationRegion");
                    String stateCode = (String) object.get("locationState");
                    String cityCode = (String) object.get("locationCity");
                    String region = regionMap.get(regionCode).getName();
                    String state = regionMap.get(regionCode).getStateMap().get(stateCode).getName();
                    String city = regionMap.get(regionCode).getStateMap().get(stateCode).getCityMap().get(cityCode).getName();
                    object.put("locationRegion", region);
                    object.put("locationState", state);
                    object.put("locationCity", city);
                }
                if (object.containsKey("hometownRegion")) {
                    Map<String, Region> regionMap = LocationUtils.getRegionMap();
                    String regionCode = (String) object.get("hometownRegion");
                    String stateCode = (String) object.get("hometownState");
                    String cityCode = (String) object.get("hometownCity");
                    String region = regionMap.get(regionCode).getName();
                    String state = regionMap.get(regionCode).getStateMap().get(stateCode).getName();
                    String city = regionMap.get(regionCode).getStateMap().get(stateCode).getCityMap().get(cityCode).getName();
                    object.put("hometownRegion", region);
                    object.put("hometownState", state);
                    object.put("hometownCity", city);
                }
            }

            //生成JSON文件
            userDataInputStreamMap.put("data.json", new ByteArrayInputStream(JSON.toJSONStringWithDateFormat(map, "yyyy-MM-dd HH:mm:ss")
                    .getBytes(StandardCharsets.UTF_8.displayName())));

            //生成UUID序列号
            String uuid = UUID.randomUUID().toString().replace("-", "");

            //压缩打包所有用户数据
            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            for (Map.Entry<String, InputStream> entry : userDataInputStreamMap.entrySet()) {
                zipOutputStream.putNextEntry(new ZipEntry(entry.getKey()));
                int length;
                byte[] buffer = new byte[1024];
                while ((length = entry.getValue().read(buffer)) != -1) {
                    zipOutputStream.write(buffer, 0, length);
                }
                zipOutputStream.closeEntry();
                entry.getValue().close();
            }
            zipOutputStream.close();

            //上传文件
            ossClient.putObject("gdeiassistant-userdata", "export/" + uuid + ".zip", new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));

            //导出用户数据成功，写入Redis记录
            exportDataDao.SaveExportDataToken(username, uuid);
            //移除导出任务记录
            exportDataDao.RemoveExportingDataToken(username);

        } finally {
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException ignored) {

                }
            }
            for (Map.Entry<String, InputStream> entry : userDataInputStreamMap.entrySet()) {
                if (entry.getValue() != null) {
                    try {
                        entry.getValue().close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            ossClient.shutdown();
        }
    }

    /**
     * 获取用户数据下载地址
     *
     * @param username
     * @return
     */
    public String DownloadUserData(String username) {
        String token = exportDataDao.QueryExportDataToken(username);
        String url = null;
        if (StringUtils.isNotBlank(token)) {
            // 创建OSSClient实例
            OSSClient ossClient = new OSSClient(endpoint, accessKeyID, accessKeySecret);
            //检查用户数据是否存在
            if (ossClient.doesObjectExist("gdeiassistant-userdata", "export/" + token + ".zip")) {
                //设置过期时间10分钟
                Date expiration = new Date(new Date().getTime() + 1000 * 60 * 90);
                // 生成URL
                url = ossClient.generatePresignedUrl("gdeiassistant-userdata", "export/" + token
                        + ".zip", expiration).toString().replace("http", "https");
            }
            ossClient.shutdown();
        }
        return url;
    }

    /**
     * 同步用户数据
     *
     * @param user
     * @return
     */
    @Transactional("appTransactionManager")
    public void SyncUserData(User user) throws Exception {
        User encryptUser = user.encryptUser();
        //检测数据库中有无该用户记录
        User queryUser = userMapper.selectUser(encryptUser.getUsername());
        if (queryUser != null) {
            //该用户已经存在,检查是否需要更新用户数据
            queryUser = queryUser.decryptUser();
            if (!queryUser.getUsername().equals(user.getUsername())
                    || !queryUser.getPassword().equals(user.getPassword())) {
                userMapper.updateUser(encryptUser);
            }
        } else {
            //用户不存在,向数据库写入该用户数据
            userMapper.insertUser(encryptUser);
        }
        //个人资料初始化
        Profile profile = profileMapper.selectUserProfile(encryptUser.getUsername());
        if (profile == null) {
            profileMapper.initUserProfile(encryptUser.getUsername(), UUID.randomUUID().toString().replace("-", ""));
        }
        Introduction introduction = profileMapper.selectUserIntroduction(encryptUser.getUsername());
        if (introduction == null) {
            profileMapper.initUserIntroduction(encryptUser.getUsername());
        }
        Privacy privacy = privacyMapper.selectPrivacy(encryptUser.getUsername());
        if (privacy == null) {
            privacyMapper.initPrivacy(encryptUser.getUsername());
        }
    }


}
