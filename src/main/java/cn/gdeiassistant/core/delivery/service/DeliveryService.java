package cn.gdeiassistant.core.delivery.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DeliveryException.DeliveryOrderStateUpdatedException;
import cn.gdeiassistant.common.exception.DeliveryException.DeliveryOrderTakenException;
import cn.gdeiassistant.common.exception.DeliveryException.NoAccessUpdatingException;
import cn.gdeiassistant.common.exception.DeliveryException.SelfTradingOrderException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.delivery.converter.DeliveryConverter;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.dto.DeliveryPublishDTO;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryTradeVO;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DeliveryService {

    @Resource(name = "deliveryMapper")
    private DeliveryMapper deliveryMapper;

    @Autowired
    private DeliveryConverter deliveryConverter;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    /**
     * 分页查询订单信息
     *
     * @param start
     * @param size
     * @return
     */
    public List<DeliveryOrderVO> queryDeliveryOrderPage(Integer start, Integer size) {
        List<DeliveryOrderEntity> list = deliveryMapper.selectDeliveryOrderPage(start, size);
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return deliveryConverter.toOrderVOList(list);
    }

    /**
     * 查询当前用户的所有下单信息
     *
     * @param sessionId
     * @return
     */
    public List<DeliveryOrderVO> queryPersonalDeliveryOrder(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<DeliveryOrderEntity> list = deliveryMapper.selectDeliveryOrderByUsername(user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return deliveryConverter.toOrderVOList(list);
    }

    /**
     * 查询当前用户的所有接受的下单信息
     *
     * @param sessionId
     * @return
     */
    public List<DeliveryOrderVO> queryPersonalAcceptedDeliveryOrder(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<DeliveryOrderEntity> list = deliveryMapper.selectAcceptedDeliveryOrderByUsername(user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return deliveryConverter.toOrderVOList(list);
    }

    /**
     * 查询当前用户的所有接单信息
     *
     * @param username
     * @return
     */
    public List<DeliveryTradeVO> queryPersonalDeliveryTrade(String username) {
        List<DeliveryTradeEntity> list = deliveryMapper.selectDeliveryTradeByUsername(username);
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return deliveryConverter.toTradeVOList(list);
    }

    /**
     * 查询订单详细信息
     *
     * @param orderId
     */
    public DeliveryOrderVO queryDeliveryOrderByOrderId(Integer orderId) throws DataNotExistException {
        DeliveryOrderEntity entity = deliveryMapper.selectDeliveryOrderByOrderId(orderId);
        if (entity == null) throw new DataNotExistException("该快递代收订单不存在");
        return deliveryConverter.toOrderVO(entity);
    }

    /**
     * 查询交易详细信息
     *
     * @param orderId
     * @return
     * @throws DataNotExistException
     */
    public DeliveryTradeVO queryDeliveryTradeByOrderId(Integer orderId) throws DataNotExistException {
        DeliveryTradeEntity entity = deliveryMapper.selectDeliveryTradeByOrderId(orderId);
        if (entity == null) throw new DataNotExistException("该快递代收订单不存在");
        return deliveryConverter.toTradeVO(entity);
    }

    /**
     * 查询交易详细信息
     *
     * @param tradeId
     * @return
     * @throws DataNotExistException
     */
    public DeliveryTradeVO queryDeliveryTradeByTradeId(Integer tradeId) throws DataNotExistException {
        DeliveryTradeEntity entity = deliveryMapper.selectDeliveryTradeByTradeId(tradeId);
        if (entity == null) throw new DataNotExistException("该快递代收交易不存在");
        return deliveryConverter.toTradeVO(entity);
    }

    /**
     * 查询当前用户组对于代收交易详细信息的权限类型
     * <p>
     * 返回的数值：DetailType表示信息详细程度
     * 0表示显示所有信息且显示确认交付按钮（下单者）
     * 1表示没有权限查看（第三方）
     * 2表示显示所有信息但没有操作按钮（接单者）
     *
     * @param sessionId
     * @param tradeId
     * @return
     * @throws DataNotExistException
     */
    public int queryDeliveryTradeDetailType(String sessionId, int tradeId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DeliveryTradeVO deliveryTrade = queryDeliveryTradeByTradeId(tradeId);
        if (deliveryTrade.getUsername().equals(user.getUsername())) {
            //自己接受的交易单
            return 2;
        } else {
            //他人接受的交易单，检查当前用户是否为下单人
            if (checkOrderPublisher(deliveryTrade.getOrderId(), sessionId)) {
                //当前用户为下单人，可以查看详细信息和交付按钮
                return 0;
            } else {
                //没有权限查看
                return 1;
            }
        }
    }

    /**
     * 查询当前用户组对于快递订单详细信息的权限类型
     * <p>
     * DetailType表示信息详细程度
     * 0表示显示所有信息和删除按钮（发布者）
     * 1表示显示大致信息和接单按钮（被接单前的第三方）
     * 2表示没有权限查看（被接单后的第三方）
     * 3表示显示所有信息但是没有操作按钮（接单者）
     *
     * @param sessionId
     * @param orderId
     * @throws DataNotExistException
     */
    public int queryDeliveryOrderDetailType(String sessionId, int orderId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DeliveryOrderVO deliveryOrder = queryDeliveryOrderByOrderId(orderId);
        if (deliveryOrder.getUsername().equals(user.getUsername())) {
            return 0;
        } else {
            if (deliveryOrder.getState().equals(0)) {
                return 1;
            } else {
                if (checkOrderAccepter(deliveryOrder.getOrderId(), sessionId)) {
                    //当前用户为接单人，可以查看详细信息
                    return 3;
                } else {
                    //没有权限查看
                    return 2;
                }
            }
        }
    }

    /**
     * 用户抢单
     *
     * @param orderId
     * @param sessionId
     */
    public void acceptOrder(Integer orderId, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        if (checkAcceptUsername(orderId, user.getUsername())) {
            throw new SelfTradingOrderException("不能接受自己下的快递代收单");
        }
        updateOrderAndInsertTradeRecord(orderId, user.getUsername());
    }

    /**
     * 检查接单人和下单人的身份，防止自行接单
     */
    public boolean checkAcceptUsername(Integer orderId, String username) {
        String orderUsername = deliveryMapper.selectDeliveryOrderUsername(orderId);
        return Objects.equals(username, orderUsername);
    }

    /**
     * 接单数据层操作，更新订单状态，插入交易记录
     *
     * @param orderId
     * @param username
     */
    @Transactional("appTransactionManager")
    public void updateOrderAndInsertTradeRecord(Integer orderId, String username) throws Exception {
        //设置排他锁更新订单状态，并发情况下只允许一个线程修改该订单ID的订单状态
        int result = deliveryMapper.updateOrderState(orderId, 1);
        if (result > 0) {
            DeliveryTradeEntity trade = new DeliveryTradeEntity();
            trade.setUsername(username);
            trade.setOrderId(orderId);
            deliveryMapper.insertTradeRecord(trade);
            DeliveryOrderEntity order = deliveryMapper.selectDeliveryOrderByOrderId(orderId);
            String company = order != null && StringUtils.isNotBlank(order.getCompany()) ? order.getCompany() : "快递代收";
            interactionNotificationService.createInteractionNotification(
                    "delivery",
                    "order_accepted",
                    order != null ? order.getUsername() : null,
                    username,
                    orderId == null ? null : String.valueOf(orderId),
                    trade.getTradeId() == null ? null : String.valueOf(trade.getTradeId()),
                    "published",
                    "订单已被接单",
                    username + " 接取了你发布的 " + company + " 订单"
            );
        } else {
            //抢单失败
            throw new DeliveryOrderTakenException("抢单失败");
        }
    }

    /**
     * 检测该订单ID对应的接单人与提交的用户名是否匹配
     *
     * @param orderId
     * @param sessionId
     * @return
     * @throws DataNotExistException
     */
    public boolean checkOrderAccepter(Integer orderId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String accepterUsername = deliveryMapper.selectDeliveryTradeUsernameByOrderId(orderId);
        if (StringUtils.isBlank(accepterUsername)) {
            throw new DataNotExistException("该快递代收交易不存在");
        }
        return Objects.equals(accepterUsername, user.getUsername());
    }

    /**
     * 检测该交易ID对应的下单人与提交的用户名是否匹配
     *
     * @param orderId
     * @param sessionId
     * @return
     * @throws DataNotExistException
     */
    public boolean checkOrderPublisher(Integer orderId, String sessionId) throws DataNotExistException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        String publisherUsername = deliveryMapper.selectDeliveryOrderUsernameByOrderId(orderId);
        if (StringUtils.isBlank(publisherUsername)) {
            throw new DataNotExistException("该快递代收订单不存在");
        }
        return Objects.equals(publisherUsername, user.getUsername());
    }

    /**
     * 删除订单信息
     *
     * @param orderId
     * @param sessionId
     * @throws DataNotExistException
     * @throws NoAccessUpdatingException
     */
    @Transactional("appTransactionManager")
    public void deleteOrder(Integer orderId, String sessionId) throws DataNotExistException, NoAccessUpdatingException, DeliveryOrderStateUpdatedException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DeliveryOrderEntity deliveryOrder = deliveryMapper.selectDeliveryOrderByOrderId(orderId);
        if (deliveryOrder != null) {
            if (deliveryOrder.getUsername().equals(user.getUsername())) {
                int result = deliveryMapper.deleteOrder(orderId);
                if (result == 0) {
                    throw new DeliveryOrderStateUpdatedException("订单状态已更新");
                }
                return;
            }
            throw new NoAccessUpdatingException("你没有权限修改该订单状态");
        }
        throw new DataNotExistException("该快递代收订单不存在");
    }

    /**
     * 完成快递代收交易
     *
     * @param tradeId
     * @param sessionId
     * @throws DataNotExistException
     * @throws NoAccessUpdatingException
     */
    @Transactional("appTransactionManager")
    public void finishTrade(Integer tradeId, String sessionId) throws DataNotExistException, NoAccessUpdatingException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DeliveryTradeEntity deliveryTrade = deliveryMapper.selectDeliveryTradeByTradeId(tradeId);
        if (deliveryTrade != null) {
            if (deliveryTrade.getOrderId() != null) {
                DeliveryOrderEntity deliveryOrder = deliveryMapper.selectDeliveryOrderByOrderId(deliveryTrade.getOrderId());
                if (deliveryOrder != null) {
                    if (deliveryOrder.getUsername().equals(user.getUsername())) {
                        int orderResult = deliveryMapper.finishOrder(deliveryTrade.getOrderId());
                        if (orderResult <= 0) {
                            return;
                        }
                        int tradeResult = deliveryMapper.updateTradeState(tradeId, 0, 1);
                        if (tradeResult <= 0) {
                            throw new IllegalStateException("快递代收交易状态更新失败");
                        }
                        String company = StringUtils.isNotBlank(deliveryOrder.getCompany()) ? deliveryOrder.getCompany() : "快递代收";
                        interactionNotificationService.createInteractionNotification(
                                "delivery",
                                "order_finished",
                                deliveryTrade.getUsername(),
                                user.getUsername(),
                                deliveryTrade.getOrderId() == null ? null : String.valueOf(deliveryTrade.getOrderId()),
                                tradeId == null ? null : String.valueOf(tradeId),
                                "accepted",
                                "订单已完成",
                                user.getUsername() + " 已确认你接取的 " + company + " 订单完成"
                        );
                        return;
                    }
                    throw new NoAccessUpdatingException("你没有权限修改该订单状态");
                }
            }
        }
        throw new DataNotExistException("该快递代收交易不存在");
    }

    /**
     * 添加快递代收订单，下单操作
     */
    public void addDeliveryOrder(String sessionId, DeliveryPublishDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DeliveryOrderEntity order = new DeliveryOrderEntity();
        order.setUsername(user.getUsername());
        order.setName(dto.getName());
        order.setNumber(dto.getNumber());
        order.setPhone(dto.getPhone());
        order.setPrice(dto.getPrice());
        order.setCompany(dto.getCompany());
        order.setAddress(dto.getAddress());
        order.setRemarks(dto.getRemarks());
        deliveryMapper.insertDeliveryOrder(order);
    }
}
