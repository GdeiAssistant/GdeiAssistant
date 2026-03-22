package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.common.pojo.Entity.*;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.phone.pojo.entity.PhoneEntity;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.phone.mapper.PhoneMapper;
import cn.gdeiassistant.core.privacy.mapper.PrivacyMapper;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.user.pojo.entity.UserEntity;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountDeletionService {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private GradeDao gradeDao;

    @Autowired
    private ScheduleDao scheduleDao;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MarketplaceMapper marketplaceMapper;

    @Autowired
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private CetMapper cetMapper;

    @Autowired
    private PhoneMapper phoneMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Autowired
    private CloseMapper closeMapper;

    @Autowired(required = false)
    private DatingMapper datingMapper;

    @Autowired(required = false)
    private cn.gdeiassistant.core.topic.mapper.TopicMapper topicMapper;

    @Autowired(required = false)
    private cn.gdeiassistant.core.secret.mapper.SecretMapper secretMapper;

    @Autowired(required = false)
    private cn.gdeiassistant.core.photograph.mapper.PhotographMapper photographMapper;

    /**
     * 关闭待处理的社区功能信息
     */
    @Transactional("appTranscationManager")
    public void CloseSocialDataState(String username) throws Exception {
        List<MarketplaceItemEntity> secondhandItemList = marketplaceMapper
                .selectItemsByUsername(username);
        for (MarketplaceItemEntity secondhandItem : secondhandItemList) {
            if (secondhandItem.getState().equals(1)) {
                marketplaceMapper.updateItemState(secondhandItem.getId(), 3);
            }
        }
        List<LostAndFoundItemEntity> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(username);
        for (LostAndFoundItemEntity lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(0)) {
                lostAndFoundMapper.updateItemState(lostAndFoundItem.getId(), 2);
            }
        }
        List<DeliveryOrderEntity> deliveryOrderList = deliveryMapper
                .selectDeliveryOrderByUsername(username);
        for (DeliveryOrderEntity deliveryOrder : deliveryOrderList) {
            if (deliveryOrder.getState().equals(0)) {
                deliveryMapper.updateOrderState(deliveryOrder.getOrderId(), 3);
            } else if (deliveryOrder.getState().equals(1)) {
                DeliveryTradeEntity deliveryTrade = deliveryMapper.selectDeliveryTradeByOrderId(deliveryOrder.getOrderId());
                if (deliveryTrade != null && deliveryTrade.getState().equals(0)) {
                    deliveryMapper.updateTradeState(deliveryTrade.getTradeId(), 0, 2);
                }
            }
        }
        //隐藏用户的交友资料
        if (datingMapper != null) {
            datingMapper.hideDatingProfilesByUsername(username);
        }
    }

    /**
     * 检查账号是否符合删除条件，若不符合，则输出所有不符合的条件，若符合则通过
     *
     * @param sessionId
     * @param password
     * @return
     */
    public DataJsonResult<Map<String, String>> checkAccountDeletability(String sessionId, String password)
            throws UserNotExistException, PasswordIncorrectException {
        Map<String, String> map = new HashMap<>();
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        //检查用户账号状态
        UserEntity queryUser = userMapper.selectUser(user.getUsername());
        if (queryUser == null) {
            //若账号不存在，则抛出异常
            throw new UserNotExistException("用户账号不存在");
        }
        if (!queryUser.getPassword().equals(password)) {
            //账号密码错误
            throw new PasswordIncorrectException("用户账号密码不匹配");
        }
        //检查有无待处理的社区功能信息
        List<MarketplaceItemEntity> secondhandItemList = marketplaceMapper
                .selectItemsByUsername(user.getUsername());
        for (MarketplaceItemEntity secondhandItem : secondhandItemList) {
            if (secondhandItem.getState().equals(1)) {
                map.put("二手交易平台存在未交易完成的物品", "请下架或确认出售账号下的所有二手交易物品");
            }
        }
        List<LostAndFoundItemEntity> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(user.getUsername());
        for (LostAndFoundItemEntity lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(0)) {
                map.put("失物招领平台存在未确认状态的物品", "请确认寻回账号下的所有失物招领物品");
            }
        }
        //检查有无待处理的全民快递订单和交易
        List<DeliveryOrderEntity> deliveryOrderList = deliveryMapper
                .selectDeliveryOrderByUsername(user.getUsername());
        for (DeliveryOrderEntity deliveryOrder : deliveryOrderList) {
            if (deliveryOrder.getState().equals(0)) {
                map.put("全民快递平台存在未删除的订单信息", "请删除账号下的所有快递代收订单信息");
            } else if (deliveryOrder.getState().equals(1)) {
                DeliveryTradeEntity deliveryTrade = deliveryMapper.selectDeliveryTradeByOrderId(deliveryOrder.getOrderId());
                if (deliveryTrade != null && deliveryTrade.getState().equals(0)) {
                    map.put("全民快递平台存在未确认交易的订单信息", "请删除账号下的所有快递代收订单信息");
                }
            }
        }
        if (map.isEmpty()) {
            return new DataJsonResult<>(true);
        }
        return new DataJsonResult<>(false, map);
    }

    /**
     * 删除用户账号
     *
     * @param sessionId
     * @throws Exception
     */
    @Transactional("appTransactionManager")
    public void deleteAccount(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        //开始进行账号关闭事务

        //删除四六级准考证号
        CetNumber cetNumber = cetMapper.selectNumber(user.getUsername());
        if (cetNumber != null) {
            cetMapper.updateNumber(user.getUsername(), null);
        }
        //删除绑定的手机号
        PhoneEntity phone = phoneMapper.selectPhone(user.getUsername());
        if (phone != null) {
            phoneMapper.deletePhone(user.getUsername());
        }
        //删除教务缓存信息
        gradeDao.removeGrade(user.getUsername());
        scheduleDao.removeSchedule(user.getUsername());
        //隐藏用户的交友资料
        if (datingMapper != null) {
            datingMapper.hideDatingProfilesByUsername(user.getUsername());
        }
        //删除用户资料信息
        profileMapper.resetUserProfile(user.getUsername(), "已注销");
        profileMapper.resetUserIntroduction(user.getUsername());
        //重置用户隐私配置
        privacyMapper.resetPrivacy(user.getUsername());
        //删除用户头像
        userProfileService.deleteAvatar(user.getUsername());
        //删除用户账号信息
        Integer count = userMapper.selectDeletedUserCount("del_"
                + StringEncryptUtils.sha1HexString(user.getUsername()).substring(0, 15));
        count = count == null ? 0 : count;
        String deletedUsername = "del_" + StringEncryptUtils.sha1HexString(user.getUsername())
                .substring(0, 15) + "_" + count;
        //匿名化社区内容的 username
        if (topicMapper != null) {
            topicMapper.anonymizeUsername(user.getUsername(), deletedUsername);
        }
        if (secretMapper != null) {
            secretMapper.anonymizeUsername(user.getUsername(), deletedUsername);
        }
        if (photographMapper != null) {
            photographMapper.anonymizeUsername(user.getUsername(), deletedUsername);
        }
        userMapper.closeUser(deletedUsername, user.getUsername());
        //保存注销日志
        SaveCloseLog(user.getUsername(), count);
    }

    /**
     * 记录账号关闭日志
     *
     * @param username
     * @param count
     */
    private void SaveCloseLog(String username, int count) {
        CloseLog closeLog = new CloseLog();
        closeLog.setUsername(username);
        closeLog.setResetname("del_" + StringEncryptUtils.sha1HexString(username)
                .substring(0, 15) + "_" + count);
        closeMapper.insertCloseLog(closeLog);
    }
}
