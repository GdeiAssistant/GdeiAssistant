package cn.gdeiassistant.Service.Account;

import cn.gdeiassistant.Exception.CloseAccountException.ItemAvailableException;
import cn.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import cn.gdeiassistant.Pojo.Entity.*;
import cn.gdeiassistant.Repository.Mongodb.Grade.GradeDao;
import cn.gdeiassistant.Repository.Mongodb.Schedule.ScheduleDao;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Cet.CetMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou.ErshouMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.LostAndFound.LostAndFoundMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone.PhoneMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Privacy.PrivacyMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Profile.ProfileMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.User.UserMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.WechatUser.WechatUserMapper;
import cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Close.CloseMapper;
import cn.gdeiassistant.Service.Profile.UserProfileService;
import cn.gdeiassistant.Service.UserLogin.UserCertificateService;
import cn.gdeiassistant.Tools.Utils.StringEncryptUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CloseAccountService {

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
    private ErshouMapper ershouMapper;

    @Autowired
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private DeliveryMapper deliveryMapper;

    @Autowired
    private CetMapper cetMapper;

    @Autowired
    private PhoneMapper phoneMapper;

    @Autowired
    private WechatUserMapper wechatUserMapper;

    @Autowired
    private ProfileMapper profileMapper;

    @Autowired
    private PrivacyMapper privacyMapper;

    @Autowired
    private CloseMapper closeMapper;

    /**
     * 关闭待处理的社区功能信息
     */
    @Transactional("appTranscationManager")
    public void CloseSocialDataState(String username) throws Exception {
        List<ErshouItem> ershouItemList = ershouMapper
                .selectItemsByUsername(username);
        for (ErshouItem ershouItem : ershouItemList) {
            if (ershouItem.getState().equals(1)) {
                ershouMapper.updateItemState(ershouItem.getId(), 3);
            }
        }
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(username);
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(0)) {
                lostAndFoundMapper.updateItemState(lostAndFoundItem.getId(), 2);
            }
        }
        List<DeliveryOrder> deliveryOrderList = deliveryMapper
                .selectDeliveryOrderByUsername(username);
        for (DeliveryOrder deliveryOrder : deliveryOrderList) {
            if (deliveryOrder.getState().equals(0)) {
                //下单者有未删除的快递代收订单信息
                deliveryMapper.updateOrderState(deliveryOrder.getOrderId(), 3);
            } else if (deliveryOrder.getState().equals(1)) {
                //下单者有未确认交付的快递代收交易
                DeliveryTrade deliveryTrade = deliveryMapper.selectDeliveryTradeByOrderId(deliveryOrder.getOrderId());
                if (deliveryTrade != null && deliveryTrade.getState().equals(0)) {
                    deliveryMapper.updateTradeState(deliveryTrade.getTradeId(), 2);
                }
            }
        }
    }


    /**
     * 删除用户账号
     *
     * @param sessionId
     * @param password
     * @throws Exception
     */
    @Transactional("appTransactionManager")
    public void CloseAccount(String sessionId, String password) throws Exception {
        User user = userCertificateService.GetUserLoginCertificate(sessionId);
        //检查用户账号状态
        User queryUser = userMapper.selectUser(user.getUsername());
        if (queryUser == null) {
            //若账号不存在，则抛出异常
            throw new UserNotExistException("用户账号不存在");
        }
        if (!queryUser.getPassword().equals(password)) {
            //账号密码错误
            throw new PasswordIncorrectException("用户账号密码不匹配");
        }
        //检查有无待处理的社区功能信息
        List<ErshouItem> ershouItemList = ershouMapper
                .selectItemsByUsername(user.getUsername());
        for (ErshouItem ershouItem : ershouItemList) {
            if (ershouItem.getState().equals(1)) {
                throw new ItemAvailableException("请下架或确认出售账号下的所有二手交易物品");
            }
        }
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundMapper
                .selectItemByUsername(user.getUsername());
        for (LostAndFoundItem lostAndFoundItem : lostAndFoundItemList) {
            if (lostAndFoundItem.getState().equals(0)) {
                throw new ItemAvailableException("请确认寻回账号下的所有失物招领物品");
            }
        }
        //检查有无待处理的全民快递订单和交易
        List<DeliveryOrder> deliveryOrderList = deliveryMapper
                .selectDeliveryOrderByUsername(user.getUsername());
        for (DeliveryOrder deliveryOrder : deliveryOrderList) {
            if (deliveryOrder.getState().equals(0)) {
                //下单者有未删除的快递代收订单信息
                throw new ItemAvailableException("请删除账号下的所有快递代收订单信息");
            } else if (deliveryOrder.getState().equals(1)) {
                //下单者有未确认交付的快递代收交易
                DeliveryTrade deliveryTrade = deliveryMapper.selectDeliveryTradeByOrderId(deliveryOrder.getOrderId());
                if (deliveryTrade != null && deliveryTrade.getState().equals(0)) {
                    throw new ItemAvailableException("请确认交付账号下的未交付的快递代收交易");
                }
            }
        }

        //开始进行账号关闭事务

        //删除四六级准考证号
        CetNumber cetNumber = cetMapper.selectNumber(user.getUsername());
        if (cetNumber != null) {
            cetMapper.updateNumber(user.getUsername(), null);
        }
        //删除绑定的手机号
        Phone phone = phoneMapper.selectPhone(user.getUsername());
        if (phone != null) {
            phoneMapper.deletePhone(user.getUsername());
        }
        //删除教务缓存信息
        gradeDao.removeGrade(user.getUsername());
        scheduleDao.removeSchedule(user.getUsername());
        //移除易班和微信绑定状态
        wechatUserMapper.resetWechatUser(user.getUsername());
        //删除用户资料信息
        profileMapper.resetUserProfile(user.getUsername(), "已注销");
        profileMapper.resetUserIntroduction(user.getUsername());
        //重置用户隐私配置
        privacyMapper.resetPrivacy(user.getUsername());
        //删除用户头像
        userProfileService.DeleteAvatar(user.getUsername());
        //删除用户账号信息
        Integer count = userMapper.selectDeletedUserCount("del_"
                + StringEncryptUtils.SHA1HexString(user.getUsername()).substring(0, 15));
        count = count == null ? 0 : count;
        userMapper.closeUser("del_" + StringEncryptUtils.SHA1HexString(user.getUsername())
                .substring(0, 15) + "_" + count, user.getUsername());
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
        closeLog.setResetname("del_" + StringEncryptUtils.SHA1HexString(username)
                .substring(0, 15) + "_" + count);
        closeMapper.insertCloseLog(closeLog);
    }
}
