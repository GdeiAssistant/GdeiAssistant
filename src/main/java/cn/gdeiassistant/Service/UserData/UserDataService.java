package cn.gdeiassistant.Service.UserData;

import cn.gdeiassistant.Constant.ItemConstantUtils;
import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Repository.Redis.ExportData.ExportDataDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Data.AppDataMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone.PhoneMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User.UserMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Data.LogDataMapper;
import cn.gdeiassistant.Service.Profile.UserProfileService;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.SpringUtils.OSSUtils;
import cn.gdeiassistant.Tools.Utils.LocationUtils;
import cn.gdeiassistant.Tools.Utils.ReflectionUtils;
import cn.gdeiassistant.Tools.Utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.taobao.wsgsvr.WsgException;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class UserDataService {

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
    private OSSUtils ossUtils;

    /**
     * 检查24小时内有无导出用户数据的记录
     *
     * @param sessionId
     * @return
     */
    public boolean CheckAlreadyExportUserData(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.QueryExportDataToken(user.getUsername()));
    }

    /**
     * 检查当前用户是否已有导出数据任务
     *
     * @param sessionId
     * @return
     */
    public boolean CheckExportingUserData(String sessionId) {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.QueryExportingDataToken(user.getUsername()));
    }

    /**
     * 导出用户数据
     *
     * @param sessionId
     * @return
     */
    @SuppressWarnings("unchecked")
    @Async
    public void ExportUserData(String sessionId) throws WsgException, IOException {

        User user = userCertificateService.GetUserLoginCertificate(sessionId);

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
            InputStream avatar = ossUtils.DownloadOSSObject("gdeiassistant-userdata"
                    , "avatar/" + user.getUsername() + ".jpg");
            if (avatar != null) {
                userDataMap.put("avatar.jpg", avatar);
            }
            //下载用户高清头像
            InputStream avatarHD = ossUtils.DownloadOSSObject("gdeiassistant-userdata"
                    , "avatar/" + user.getUsername() + "_hd.jpg");
            if (avatarHD != null) {
                userDataMap.put("avatar_hd.jpg", avatarHD);
            }
            //获取绑定手机信息
            Phone phone = phoneMapper.selectPhone(user.getUsername());
            if (phone != null) {
                phone.setPhone(phone.getPhone());
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
            Profile profile = appDataMapper.selectUserProfile(user.getUsername());
            if (profile != null) {
                data.put("profile", profile);
            }
            //获取个人简介信息
            Introduction introduction = appDataMapper.selectUserIntroduction(user.getUsername());
            if (introduction != null && introduction.getIntroductionContent() != null) {
                data.put("introduction", introduction.getIntroductionContent());
            }
            //获取用户隐私设置
            Privacy privacy = appDataMapper.selectUserPrivacy(user.getUsername());
            if (privacy != null) {
                data.put("privacy", privacy);
            }
            //获取保存的四六级准考证号
            CetNumber cetNumber = appDataMapper.selectUserCetNumber(user.getUsername());
            if (cetNumber != null && cetNumber.getNumber() != null) {
                data.put("cet", cetNumber);
            }
            //获取全民快递订单信息
            List<DeliveryOrder> deliveryOrderList = appDataMapper.selectUserDeliveryOrderList(user.getUsername());
            if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
                data.put("deliveryOrders", deliveryOrderList);
            }
            //获取全名快递交易信息
            List<DeliveryTrade> deliveryTradeList = appDataMapper.selectUserDeliveryTradeList(user.getUsername());
            if (deliveryTradeList != null && !deliveryTradeList.isEmpty()) {
                data.put("deliveryTrades", deliveryTradeList);
            }
            //获取二手交易信息
            List<ErshouItem> ershouItemList = appDataMapper.selectUserErshouItemList(user.getUsername());
            if (ershouItemList != null && !ershouItemList.isEmpty()) {
                for (ErshouItem ershouItem : ershouItemList) {
                    //下载二手交易图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = ossUtils.DownloadOSSObject("gdeiassistant-userdata", "ershou/"
                                + ershouItem.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("ershou_" + ershouItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("ershouItems", ershouItemList);
            }
            //获取失物招领信息
            List<LostAndFoundItem> lostAndFoundItemList = appDataMapper.selectUserLostAndFoundItemList(user.getUsername());
            if (lostAndFoundItemList != null && !lostAndFoundItemList.isEmpty()) {
                for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
                    //下载失物招领图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = ossUtils.DownloadOSSObject("gdeiassistant-userdata", "lostandfound/"
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
            List<Secret> secretList = appDataMapper.selectUserSecretItemList(user.getUsername());
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
                        InputStream voice = ossUtils.DownloadOSSObject("gdeiassistant-userdata", "secret/voice/" + secret.getId() + ".mp3");
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
            List<Photograph> photographList = appDataMapper.selectUserPhotographItemList(user.getUsername());
            if (photographList != null && !photographList.isEmpty()) {
                for (Photograph photograph : photographList) {
                    //下载拍好校园图片
                    for (int i = 1; i <= photograph.getCount(); i++) {
                        InputStream image = ossUtils.DownloadOSSObject("gdeiassistant-userdata", "photograph/"
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
            List<Express> expressList = appDataMapper.selectUserExpresssItemList(user.getUsername());
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
            userDataMap.put("data.json", new ByteArrayInputStream(JSON.toJSONStringWithDateFormat(map, "yyyy-MM-dd HH:mm:ss")
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
            ossUtils.UploadOSSObject("gdeiassistant-userdata", "export/" + uuid + ".zip"
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
                        e.printStackTrace();
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
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        String token = exportDataDao.QueryExportDataToken(user.getUsername());
        String url = null;
        if (StringUtils.isNotBlank(token)) {
            ossUtils.GeneratePresignedUrl("gdeiassistant-userdata", "export/" + token + ".zip"
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
    public void SyncUserData(User user) throws Exception {
        //检测数据库中有无该用户记录
        User queryUser = userMapper.selectUser(user.getUsername());
        if (queryUser != null) {
            //该用户已经存在,检查是否需要更新用户数据
            if (queryUser.equals(user)) {
                userMapper.updateUser(user);
            }
        } else {
            //用户不存在,向数据库写入该用户数据
            userMapper.insertUser(user);
        }
        //个人资料初始化
        Profile profile = profileMapper.selectUserProfile(user.getUsername());
        if (profile == null) {
            profileMapper.initUserProfile(user.getUsername(), UUID.randomUUID().toString().replace("-", ""));
        }
        Introduction introduction = profileMapper.selectUserIntroduction(user.getUsername());
        if (introduction == null) {
            profileMapper.initUserIntroduction(user.getUsername());
        }
        Privacy privacy = privacyMapper.selectPrivacy(user.getUsername());
        if (privacy == null) {
            privacyMapper.initPrivacy(user.getUsername());
        }
    }


}
