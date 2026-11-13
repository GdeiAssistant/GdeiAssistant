package cn.gdeiassistant.core.userdata.service;

import org.springframework.beans.factory.annotation.Qualifier;

import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
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
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.common.redis.exportdata.ExportDataDao;
import cn.gdeiassistant.core.data.mapper.AppDataMapper;
import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.common.exception.AccountCleanupPendingException;
import cn.gdeiassistant.core.logdata.mapper.LogDataMapper;
import cn.gdeiassistant.core.secret.service.SecretService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.common.tools.utils.AnonymizeUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;

@Service
public class UserDataService {

    private static final Logger logger = LoggerFactory.getLogger(UserDataService.class);
    private final UserCertificateService userCertificateService;

    private final DeletionCleanupMapper deletionCleanupMapper;

    private final UserMapper userMapper;

    private final PhoneMapper phoneMapper;

    private final ProfileMapper profileMapper;

    private final PrivacyMapper privacyMapper;

    private final AppDataMapper appDataMapper;

    private final LogDataMapper logDataMapper;

    private final ExportDataDao exportDataDao;

    private final R2StorageService r2StorageService;

    private final cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;

    private final SecretService secretService;

    private final List<AccountLifecycleContributor> contributors;

    public UserDataService(UserCertificateService userCertificateService, DeletionCleanupMapper deletionCleanupMapper, @Qualifier("userMapper") UserMapper userMapper, @Qualifier("phoneMapper") PhoneMapper phoneMapper, @Qualifier("profileMapper") ProfileMapper profileMapper, @Qualifier("privacyMapper") PrivacyMapper privacyMapper, @Qualifier("appDataMapper") AppDataMapper appDataMapper, @Qualifier("logDataMapper") LogDataMapper logDataMapper, ExportDataDao exportDataDao, R2StorageService r2StorageService, cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets, SecretService secretService, List<AccountLifecycleContributor> contributors) {
        this.userCertificateService = userCertificateService;
        this.deletionCleanupMapper = deletionCleanupMapper;
        this.userMapper = userMapper;
        this.phoneMapper = phoneMapper;
        this.profileMapper = profileMapper;
        this.privacyMapper = privacyMapper;
        this.appDataMapper = appDataMapper;
        this.logDataMapper = logDataMapper;
        this.exportDataDao = exportDataDao;
        this.r2StorageService = r2StorageService;
        this.storedAssets = storedAssets;
        this.secretService = secretService;
        this.contributors = contributors;
    }

    /**
     * 检查24小时内有无导出用户数据的记录
     *
     * @param sessionId
     * @return
     */
    public boolean checkAlreadyExportUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.queryExportDataToken(user.getUsername()));
    }

    /**
     * 检查当前用户是否已有导出数据任务
     *
     * @param sessionId
     * @return
     */
    public boolean checkExportingUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return StringUtils.isNotBlank(exportDataDao.queryExportingDataToken(user.getUsername()));
    }

    /**
     * 导出用户数据
     *
     * @param sessionId
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @Async
    public void exportUserData(String sessionId) throws IOException {

        User user = userCertificateService.getUserLoginCertificate(sessionId);

        //写入导出任务记录
        exportDataDao.saveExportingDataToken(user.getUsername(), UUID.randomUUID().toString());

        ByteArrayInputStream byteArrayInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        ZipOutputStream zipOutputStream = null;
        Map<String, InputStream> userDataMap = new HashMap<>();

        try {
            Map<String, Object> data = new HashMap<>();
            data.put("username", user.getUsername());
            //下载用户头像
            InputStream avatar = r2StorageService.downloadObject(null
                    , "avatar/" + user.getUsername() + ".jpg");
            if (avatar != null) {
                userDataMap.put("avatar.jpg", avatar);
            }
            //下载用户高清头像
            InputStream avatarHD = r2StorageService.downloadObject(null
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
                data.put("phone", UserDataExportProjection.phone(phone));
            }
            //获取个人资料信息
            ProfileEntity profile = appDataMapper.selectUserProfile(user.getUsername());
            if (profile != null) {
                data.put("profile", UserDataExportProjection.profile(profile));
            }
            //获取个人简介信息
            Introduction introduction = appDataMapper.selectUserIntroduction(user.getUsername());
            if (introduction != null && introduction.getIntroductionContent() != null) {
                data.put("introduction", introduction.getIntroductionContent());
            }
            //获取用户隐私设置
            PrivacyEntity privacy = appDataMapper.selectUserPrivacy(user.getUsername());
            if (privacy != null) {
                data.put("privacy", UserDataExportProjection.privacy(privacy));
            }
            //获取保存的四六级准考证号
            CetNumberEntity cetNumber = appDataMapper.selectUserCetNumber(user.getUsername());
            if (cetNumber != null && cetNumber.getNumber() != null) {
                data.put("cet", UserDataExportProjection.cet(cetNumber));
            }
            //获取全民快递订单信息
            List<DeliveryOrderEntity> deliveryOrderList = appDataMapper.selectUserDeliveryOrderList(user.getUsername());
            if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
                data.put("deliveryOrders", deliveryOrderList.stream().map(UserDataExportProjection::deliveryOrder).toList());
            }
            //获取全名快递交易信息
            List<DeliveryTradeEntity> deliveryTradeList = appDataMapper.selectUserDeliveryTradeList(user.getUsername());
            if (deliveryTradeList != null && !deliveryTradeList.isEmpty()) {
                data.put("deliveryTrades", deliveryTradeList.stream().map(UserDataExportProjection::deliveryTrade).toList());
            }
            //获取二手交易信息
            List<MarketplaceItemEntity> secondhandItemList = appDataMapper.selectUserErshouItemList(user.getUsername());
            if (secondhandItemList != null && !secondhandItemList.isEmpty()) {
                for (MarketplaceItemEntity secondhandItem : secondhandItemList) {
                    //下载二手交易图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = r2StorageService.downloadObject(null, "ershou/"
                                + secondhandItem.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("ershou_" + secondhandItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("ershouItems", secondhandItemList.stream().map(UserDataExportProjection::marketplaceItem).toList());
            }
            //获取失物招领信息
            List<LostAndFoundItemEntity> lostAndFoundItemList = appDataMapper.selectUserLostAndFoundItemList(user.getUsername());
            if (lostAndFoundItemList != null && !lostAndFoundItemList.isEmpty()) {
                for (LostAndFoundItemEntity lostAndFoundItem : lostAndFoundItemList) {
                    //下载失物招领图片
                    for (int i = 0; i <= 3; i++) {
                        InputStream image = r2StorageService.downloadObject(null, "lostandfound/"
                                + lostAndFoundItem.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("lostandfound_" + lostAndFoundItem.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("lostandfoundItems", lostAndFoundItemList.stream().map(UserDataExportProjection::lostAndFoundItem).toList());
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
                        String voiceObjectKey = secretService.findSecretVoiceObjectKey(secret.getId());
                        InputStream voice = voiceObjectKey == null ? null : r2StorageService.downloadObject(null, voiceObjectKey);
                        if (voice != null) {
                            String extension = ".mp3";
                            int dotIndex = voiceObjectKey.lastIndexOf('.');
                            if (dotIndex >= 0) {
                                extension = voiceObjectKey.substring(dotIndex);
                            }
                            userDataMap.put("secret_voice_" + secret.getId() + extension, voice);
                        }
                    }
                }
                data.put("secretItems", secretList.stream().map(UserDataExportProjection::secretContent).toList());
            }
            //获取拍好校园信息
            List<PhotographEntity> photographList = appDataMapper.selectUserPhotographItemList(user.getUsername());
            if (photographList != null && !photographList.isEmpty()) {
                for (PhotographEntity photograph : photographList) {
                    //下载拍好校园图片
                    for (int i = 1; i <= photograph.getCount(); i++) {
                        InputStream image = r2StorageService.downloadObject(null, "photograph/"
                                + photograph.getId() + "_" + i + ".jpg");
                        if (image != null) {
                            userDataMap.put("photograph_" + photograph.getId() + "_" + i + ".jpg", image);
                        } else {
                            break;
                        }
                    }
                }
                data.put("photographItems", photographList.stream().map(UserDataExportProjection::photograph).toList());
            }
            //获取表白墙信息
            List<ExpressEntity> expressList = appDataMapper.selectUserExpressItemList(user.getUsername());
            if (expressList != null && !expressList.isEmpty()) {
                data.put("expressItems", expressList.stream().map(UserDataExportProjection::express).toList());
            }
            //获取校园卡充值日志记录
            List<ChargeLog> chargeLogList = logDataMapper.selectChargeLogList(user.getUsername());
            if (chargeLogList != null && !chargeLogList.isEmpty()) {
                data.put("chargeLogs", chargeLogList.stream().map(UserDataExportProjection::chargeLog).toList());
            }
            AccountDataExport export = new AccountDataExport(data);
            for (AccountLifecycleContributor contributor : contributors) {
                contributor.decorateExport(export);
            }

            //生成JSON文件
            userDataMap.put("data.json", new ByteArrayInputStream(JSON.toJSONString(data, "yyyy-MM-dd HH:mm:ss")
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
            r2StorageService.uploadObject(null, "export/" + uuid + ".zip"
                    , byteArrayInputStream);

            //导出用户数据成功，写入Redis记录
            exportDataDao.saveExportDataToken(user.getUsername(), uuid);
            //移除导出任务记录
            exportDataDao.removeExportingDataToken(user.getUsername());

        } catch (Exception e) {
            logger.error("导出用户数据失败，username={}", AnonymizeUtils.maskUsername(user.getUsername()), e);
            exportDataDao.removeExportingDataToken(user.getUsername());
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
                        logger.error("关闭用户数据导出资源流失败", e);
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
    public String downloadUserData(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String token = exportDataDao.queryExportDataToken(user.getUsername());
        String url = null;
        if (StringUtils.isNotBlank(token)) {
            url = storedAssets.generatePresignedUrl("export/" + token + ".zip"
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
    @Transactional(value="appTransactionManager", isolation=Isolation.READ_COMMITTED)
    public void syncUserData(User user) throws Exception {
        syncUserData(user, true);
    }

    /**
     * 同步用户数据，并按需决定是否刷新长期保存的校园凭证。
     *
     * @param user
     * @param persistCredential true 表示允许更新 MySQL 中保存的校园凭证；false 表示仅初始化账号相关资料
     */
    @Transactional(value="appTransactionManager", isolation=Isolation.READ_COMMITTED)
    public void syncUserData(User user, boolean persistCredential) throws Exception {
        cn.gdeiassistant.core.user.pojo.entity.CampusAccountView queryUser = userMapper.selectUser(user.getUsername());
        if (queryUser != null) {
            // Match deletion's app_user lock order and re-read after a concurrent rename/close.
            queryUser = userMapper.selectUserByIdForUpdate(queryUser.getId());
            if (queryUser != null && !user.getUsername().equals(queryUser.getUsername())) queryUser = null;
        }
        if (queryUser != null && !queryUser.isActive()) {
            // 已注销账号的校园用户名已被改名，正常不会命中；若命中则拒绝复用
            throw new IllegalStateException(Messages.get("userdata.accountClosed"));
        }
        if (queryUser == null) {
            for (String id : deletionCleanupMapper.unfinishedIdsForUsername(user.getUsername())) {
                if (deletionCleanupMapper.lockUnfinished(id) != null) {
                    throw new AccountCleanupPendingException();
                }
            }
            cn.gdeiassistant.core.user.pojo.entity.CampusAccountView entity = new cn.gdeiassistant.core.user.pojo.entity.CampusAccountView();
            entity.setPublicId(java.util.UUID.randomUUID().toString());
            entity.setStatus("ACTIVE");
            entity.setUsername(user.getUsername());
            entity.setPassword(persistCredential ? user.getPassword() : null);
            userMapper.insertAppUser(entity);
            userMapper.insertCampusCredential(entity);
        } else if (persistCredential) {
            cn.gdeiassistant.core.user.pojo.entity.CampusAccountView entity = new cn.gdeiassistant.core.user.pojo.entity.CampusAccountView();
            entity.setUsername(user.getUsername());
            entity.setPassword(user.getPassword());
            userMapper.updateUser(entity);
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
