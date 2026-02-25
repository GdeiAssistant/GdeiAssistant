package cn.gdeiassistant.core.userData.service;

import cn.gdeiassistant.common.constant.ItemConstantUtils;
import cn.gdeiassistant.core.secondhand.pojo.entity.SecondhandItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.cetquery.pojo.entity.CetNumberEntity;
import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.common.pojo.Entity.*;
import cn.gdeiassistant.common.redis.ExportData.ExportDataDao;
import cn.gdeiassistant.core.data.mapper.AppDataMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.logdata.mapper.LogDataMapper;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.ReflectionUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class UserDataService {

    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);
    @Autowired
    private UserCertificateService userCertificateService;

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

    @Autowired
    private R2StorageService r2StorageService;

    /**
     * 检查24小时内有无导出用户数据的记录
     *
     * @param sessionId
     * @return
     */
    public boolean CheckAlreadyExportUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.QueryExportDataToken(user.getUsername()));
    }

    /**
     * 检查当前用户是否已有导出数据任务
     *
     * @param sessionId
     * @return
     */
    public boolean CheckExportingUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.QueryExportingDataToken(user.getUsername()));
    }

    /**
     * 导出用户数据
     *
     * @param sessionId
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Async
    public void ExportUserData(String sessionId) throws IOException {

        User user = userCertificateService.getUserLoginCertificate(sessionId);

        //写入导出任务记录
        exportDataDao.SaveExportingDataToken(user.getUsername(), UUID.randomUUID().toString());

        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        Map<String, InputStream> userDataMap = new HashMap<>();

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            //下载用户头像
            InputStream avatar = r2StorageService.downloadObject("gdeiassistant-userdata"
                    , "avatar/" + user.getUsername() + ".jpg");
            if (avatar != null) {
                userDataMap.put("avatar.jpg", avatar);
            }
            //下载用户高清头像
            InputStream avatarHD = r2StorageService.downloadObject("gdeiassistant-userdata"
                    , "avatar/" + user.getUsername() + "_hd.jpg");
            if (avatarHD != null) {
                userDataMap.put("avatar_hd.jpg", avatarHD);
            }
            //获取绑定手机信息
            PhoneEntity phone = phoneMapper.selectPhone(user.getUsername());
            if (phone != null) {
                String raw = phone.getPhone() != null ? phone.getPhone() : "";
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < raw.length(); i++) {
                    if (i < 3) {
                        stringBuilder.append(raw.charAt(i));
                    } else {
                        stringBuilder.append('*');
                    }
                }
                phone.setPhone(stringBuilder.toString());
                data.put("phone", phone);
            }
            //获取个人资料信息
            ProfileEntity profile = appDataMapper.selectUserProfile(user.getUsername());
            if (profile != null) {
                data.put("profile", profile);
            }
            //获取个人简介信息
            Introduction introduction = appDataMapper.selectUserIntroduction(user.getUsername());
            if (introduction != null && introduction.getIntroductionContent() != null) {
                data.put("introduction", introduction.getIntroductionContent());
            }
            //获取用户隐私设置
            PrivacyEntity privacy = appDataMapper.selectUserPrivacy(user.getUsername());
            if (privacy != null) {
                data.put("privacy", privacy);
            }
            //获取保存的四六级准考证号
            CetNumberEntity cetNumber = appDataMapper.selectUserCetNumber(user.getUsername());
            if (cetNumber != null && cetNumber.getNumber() != null) {
                data.put("cet", cetNumber);
            }
            //获取全民快递订单信息
            List<DeliveryOrderEntity> deliveryOrderList = appDataMapper.selectUserDeliveryOrderList(user.getUsername());
            if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
                data.put("deliveryOrders", deliveryOrderList);
            }
            //获取全名快递交易信息
            List<DeliveryTradeEntity> deliveryTradeList = appDataMapper.selectUserDeliveryTradeList(user.getUsername());
            if (deliveryTradeList != null && !deliveryTradeList.isEmpty()) {
                data.put("deliveryTrades", deliveryTradeList);
            }
            //获取二手交易信息
            List<SecondhandItemEntity> secondhandItemList = appDataMapper.selectUserErshouItemList(user.getUsername());
            if (secondhandItemList != null && !secondhandItemList.isEmpty()) {
                for (SecondhandItemEntity secondhandItem : secondhandItemList) {
                    //下载二手交易图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = r2StorageService.downloadObject("gdeiassistant-userdata", "ershou/"
                                + secondhandItem.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("ershou_" + secondhandItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("ershouItems", secondhandItemList);
            }
            //获取失物招领信息
            List<LostAndFoundItemEntity> lostAndFoundItemList = appDataMapper.selectUserLostAndFoundItemList(user.getUsername());
            if (lostAndFoundItemList != null && !lostAndFoundItemList.isEmpty()) {
                for (LostAndFoundItemEntity lostAndFoundItem : lostAndFoundItemList) {
                    //下载失物招领图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = r2StorageService.downloadObject("gdeiassistant-userdata", "lostandfound/"
                                + lostAndFoundItem.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("lostandfound_" + lostAndFoundItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("lostandfoundItems", lostAndFoundItemList);
            }
            //获取校园树洞信息
            List<SecretContentEntity> secretList = appDataMapper.selectUserSecretItemList(user.getUsername());
            if (secretList != null && !secretList.isEmpty()) {
                for (SecretContentEntity secret : secretList) {
                    if (secret.getSecretCommentList() != null && !secret.getSecretCommentList().isEmpty()) {
                        for (SecretCommentEntity secretComment : secret.getSecretCommentList()) {
                            //隐藏树洞评论信息发布者的用户名
                            secretComment.setUsername(null);
                        }
                        //设置评论数量
                        secret.setCommentCount(secret.getSecretCommentList().size());
                    } else {
                        //设置评论数量
                        secret.setCommentCount(0);
                    }
                    if (secret.getType() != null && secret.getType().equals(1)) {
                        //下载语音信息
                        InputStream voice = r2StorageService.downloadObject("gdeiassistant-userdata", "secret/voice/" + secret.getId() + ".mp3");
                        if (voice != null) {
                            userDataMap.put("secret_voice_" + secret.getId() + ".mp3", voice);
                        } else {
                            break;
                        }
                    }
                }
                data.put("secretItems", secretList);
            }
            //获取拍好校园信息
            List<PhotographEntity> photographList = appDataMapper.selectUserPhotographItemList(user.getUsername());
            if (photographList != null && !photographList.isEmpty()) {
                for (PhotographEntity photograph : photographList) {
                    //下载拍好校园图片
                    for (int i = 1; i <= photograph.getCount(); i++) {
                        InputStream image = r2StorageService.downloadObject("gdeiassistant-userdata", "photograph/"
                                + photograph.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("photograph_" + photograph.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("photographItems", photographList);
            }
            //获取表白墙信息
            List<ExpressEntity> expressList = appDataMapper.selectUserExpressItemList(user.getUsername());
            if (expressList != null && !expressList.isEmpty()) {
                data.put("expressItems", expressList);
            }
            //获取校园卡充值日志记录
            List<ChargeLog> chargeLogList = logDataMapper.selectChargeLogList(user.getUsername());
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
            userDataMap.put("data.json", new ByteArrayInputStream(JSON.toJSONString(map, "yyyy-MM-dd HH:mm:ss")
                    .getBytes(StandardCharsets.UTF_8.displayName())));

            //生成UUID序列号
            String uuid = UUID.randomUUID().toString().replace("-", "");

            //压缩打包所有用户数据
            byteArrayOutputStream = new ByteArrayOutputStream();
            zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
            for (Map.Entry<String, InputStream> entry : userDataMap.entrySet()) {
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
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            r2StorageService.uploadObject("gdeiassistant-userdata", "export/" + uuid + ".zip"
                    , byteArrayInputStream);

            //导出用户数据成功，写入Redis记录
            exportDataDao.SaveExportDataToken(user.getUsername(), uuid);
            //移除导出任务记录
            exportDataDao.RemoveExportingDataToken(user.getUsername());

        } finally {
            if (byteArrayInputStream != null) {
                byteArrayInputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException ignored) {

                }
            }
            for (Map.Entry<String, InputStream> entry : userDataMap.entrySet()) {
                if (entry.getValue() != null) {
                    try {
                        entry.getValue().close();
                    } catch (IOException e) {
                        logger.error("关闭用户数据导出资源流失败，username={}，key={}",
                                user.getUsername(), entry.getKey(), e);
                    }
                }
            }
        }
    }

    /**
     * 获取用户数据下载地址
     *
     * @param sessionId
     * @return
     */
    public String DownloadUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String token = exportDataDao.QueryExportDataToken(user.getUsername());
        String url = null;
        if (StringUtils.isNotBlank(token)) {
            url = r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "export/" + token + ".zip"
                    , 90, TimeUnit.MINUTES);
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
    public void syncUserData(User user) throws Exception {
        cn.gdeiassistant.core.user.pojo.entity.UserEntity queryUser = userMapper.selectUser(user.getUsername());
        cn.gdeiassistant.core.user.pojo.entity.UserEntity entity = new cn.gdeiassistant.core.user.pojo.entity.UserEntity();
        entity.setUsername(user.getUsername());
        entity.setPassword(user.getPassword());
        if (queryUser != null) {
            if (queryUser.getUsername().equals(user.getUsername()) && queryUser.getPassword().equals(user.getPassword())) {
                userMapper.updateUser(entity);
            }
        } else {
            userMapper.insertUser(entity);
        }
        // 以下保持原逻辑：个人资料初始化
        ProfileEntity profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile == null) {
            profileMapper.initUserProfile(user.getUsername(), UUID.randomUUID().toString().replace("-", ""));
        }
        Introduction introduction = profileMapper.selectUserIntroduction(user.getUsername());
        if (introduction == null) {
            profileMapper.initUserIntroduction(user.getUsername());
        }
        PrivacyEntity privacy = privacyMapper.selectPrivacy(user.getUsername());
        if (privacy == null) {
            privacyMapper.initPrivacy(user.getUsername());
        }
    }
}
