package edu.gdei.gdeiassistant.Service.Delivery;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.DeliveryOrderStateUpdatedException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.DeliveryOrderTakenException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.NoAccessUpdatingException;
import edu.gdei.gdeiassistant.Exception.DeliveryException.SelfTradingOrderException;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryOrder;
import edu.gdei.gdeiassistant.Pojo.Entity.DeliveryTrade;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Delivery.DeliveryMapper;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class DeliveryService {

    @Resource(name = "deliveryMapper")
    private DeliveryMapper deliveryMapper;

    /**
     * 分页查询订单信息
     *
     * @param start
     * @param size
     * @return
     */
    public List<DeliveryOrder> QueryDeliveryOrderPage(Integer start, Integer size) throws WsgException {
        List<DeliveryOrder> deliveryOrderList = deliveryMapper.selectDeliveryOrderPage(start, size);
        if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
            for (DeliveryOrder deliveryOrder : deliveryOrderList) {
                deliveryOrder.setUsername(StringEncryptUtils.decryptString(deliveryOrder.getUsername()));
            }
            return deliveryOrderList;
        }
        return new ArrayList<>();
    }

    /**
     * 查询当前用户的所有下单信息
     *
     * @param username
     * @return
     * @throws WsgException
     */
    public List<DeliveryOrder> QueryPersonalDeliveryOrder(String username) throws WsgException {
        List<DeliveryOrder> list = new ArrayList<>();
        List<DeliveryOrder> deliveryOrderList = deliveryMapper.selectDeliveryOrderByUsername(StringEncryptUtils.encryptString(username));
        if (deliveryOrderList != null && !deliveryOrderList.isEmpty()) {
            for (DeliveryOrder deliveryOrder : deliveryOrderList) {
                deliveryOrder.setUsername(StringEncryptUtils.decryptString(deliveryOrder.getUsername()));
            }
            list.addAll(deliveryOrderList);
        }
        List<DeliveryOrder> acceptedDeliveryOrderList = deliveryMapper.selectAcceptedDeliveryOrderByUsername(StringEncryptUtils.encryptString(username));
        if (acceptedDeliveryOrderList != null && !acceptedDeliveryOrderList.isEmpty()) {
            for (DeliveryOrder deliveryOrder : acceptedDeliveryOrderList) {
                deliveryOrder.setUsername(StringEncryptUtils.decryptString(deliveryOrder.getUsername()));
            }
            list.addAll(acceptedDeliveryOrderList);
        }
        return list;
    }

    /**
     * 查询当前用户的所有接单信息
     *
     * @param username
     * @return
     */
    public List<DeliveryTrade> QueryPersonalDeliveryTrade(String username) throws WsgException {
        List<DeliveryTrade> deliveryTradeList = deliveryMapper.selectDeliveryTradeByUsername(StringEncryptUtils.encryptString(username));
        if (deliveryTradeList != null && !deliveryTradeList.isEmpty()) {
            for (DeliveryTrade deliveryTrade : deliveryTradeList) {
                deliveryTrade.setUsername(StringEncryptUtils.decryptString(deliveryTrade.getUsername()));
            }
            return deliveryTradeList;
        }
        return new ArrayList<>();
    }

    /**
     * 查询订单详细信息
     *
     * @param orderId
     */
    public DeliveryOrder QueryDeliveryOrderByOrderId(Integer orderId) throws DataNotExistException, WsgException {
        DeliveryOrder deliveryOrder = deliveryMapper.selectDeliveryOrderByOrderId(orderId);
        if (deliveryOrder != null) {
            deliveryOrder.setUsername(StringEncryptUtils.decryptString(deliveryOrder.getUsername()));
            return deliveryOrder;
        }
        throw new DataNotExistException("该快递代收订单不存在");
    }

    /**
     * 查询交易详细信息
     *
     * @param orderId
     * @return
     * @throws WsgException
     * @throws DataNotExistException
     */
    public DeliveryTrade QueryDeliveryTradeByOrderId(Integer orderId) throws WsgException, DataNotExistException {
        DeliveryTrade deliveryTrade = deliveryMapper.selectDeliveryTradeByOrderId(orderId);
        if (deliveryTrade != null) {
            deliveryTrade.setUsername(StringEncryptUtils.decryptString(deliveryTrade.getUsername()));
            return deliveryTrade;
        }
        throw new DataNotExistException("该快递代收订单不存在");
    }

    /**
     * 查询交易详细信息
     *
     * @param tradeId
     * @return
     * @throws DataNotExistException
     * @throws WsgException
     */
    public DeliveryTrade QueryDeliveryTradeByTradeId(Integer tradeId) throws DataNotExistException, WsgException {
        DeliveryTrade deliveryTrade = deliveryMapper.selectDeliveryTradeByTradeId(tradeId);
        if (deliveryTrade != null) {
            deliveryTrade.setUsername(StringEncryptUtils.decryptString(deliveryTrade.getUsername()));
            return deliveryTrade;
        }
        throw new DataNotExistException("该快递代收交易不存在");
    }

    /**
     * 用户抢单
     *
     * @param orderId
     * @param username
     */
    public void AcceptOrder(Integer orderId, String username) throws Exception {
        if (CheckAcceptUsername(orderId, username)) {
            throw new SelfTradingOrderException("不能接受自己下的快递代收单");
        }
        UpdateOrderAndInsertTradeRecord(orderId, username);
    }

    /**
     * 检查接单人和下单人的身份，防止自行接单
     */
    public boolean CheckAcceptUsername(Integer orderId, String username) throws WsgException {
        String orderUsername = deliveryMapper.selectDeliveryOrderUsername(orderId);
        return Objects.equals(StringEncryptUtils.encryptString(username), orderUsername);
    }

    /**
     * 接单数据层操作，更新订单状态，插入交易记录
     *
     * @param orderId
     * @param username
     */
    @Transactional("appTransactionManager")
    public void UpdateOrderAndInsertTradeRecord(Integer orderId, String username) throws Exception {
        //设置排他锁更新订单状态，并发情况下只允许一个线程修改该订单ID的订单状态
        int result = deliveryMapper.updateOrderState(orderId, 1);
        if (result > 0) {
            //若影响行数大于0，则表示抢单成功
            DeliveryTrade deliveryTrade = new DeliveryTrade();
            deliveryTrade.setUsername(StringEncryptUtils.encryptString(username));
            deliveryTrade.setOrderId(orderId);
            //插入交易记录
            deliveryMapper.insertTradeRecord(deliveryTrade);
        } else {
            //抢单失败
            throw new DeliveryOrderTakenException("抢单失败");
        }
    }

    /**
     * 检测该订单ID对应的接单人与提交的用户名是否匹配
     *
     * @param orderId
     * @param username
     * @return
     */
    public boolean CheckOrderAccepter(Integer orderId, String username) throws DataNotExistException, WsgException {
        String accepterUsername = deliveryMapper.selectDeliveryTradeUsernameByOrderId(orderId);
        if (StringUtils.isBlank(accepterUsername)) {
            throw new DataNotExistException("该快递代收交易不存在");
        }
        return Objects.equals(StringEncryptUtils.decryptString(accepterUsername), username);
    }

    /**
     * 检测该交易ID对应的下单人与提交的用户名是否匹配
     *
     * @param orderId
     * @param username
     * @return
     * @throws WsgException
     */
    public boolean CheckOrderPublisher(Integer orderId, String username) throws WsgException, DataNotExistException {
        String publisherUsername = deliveryMapper.selectDeliveryOrderUsernameByOrderId(orderId);
        if (StringUtils.isBlank(publisherUsername)) {
            throw new DataNotExistException("该快递代收订单不存在");
        }
        return Objects.equals(StringEncryptUtils.decryptString(publisherUsername), username);
    }

    /**
     * 删除订单信息
     *
     * @param orderId
     * @param username
     * @throws DataNotExistException
     * @throws WsgException
     * @throws NoAccessUpdatingException
     */
    @Transactional("appTransactionManager")
    public void DeleteOrder(Integer orderId, String username) throws DataNotExistException, WsgException, NoAccessUpdatingException, DeliveryOrderStateUpdatedException {
        DeliveryOrder deliveryOrder = deliveryMapper.selectDeliveryOrderByOrderId(orderId);
        if (deliveryOrder != null) {
            if (deliveryOrder.getUsername().equals(StringEncryptUtils.encryptString(username))) {
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
     */
    public void FinishTrade(Integer tradeId, String username) throws DataNotExistException, WsgException, NoAccessUpdatingException {
        DeliveryTrade deliveryTrade = deliveryMapper.selectDeliveryTradeByTradeId(tradeId);
        if (deliveryTrade != null) {
            if (deliveryTrade.getOrderId() != null) {
                DeliveryOrder deliveryOrder = deliveryMapper.selectDeliveryOrderByOrderId(deliveryTrade.getOrderId());
                if (deliveryOrder != null) {
                    if (deliveryOrder.getUsername().equals(StringEncryptUtils.encryptString(username))) {
                        deliveryMapper.updateTradeState(tradeId, 1);
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
     *
     * @param deliveryOrder
     */
    public void AddDeliveryOrder(DeliveryOrder deliveryOrder) throws WsgException {
        DeliveryOrder order = new DeliveryOrder();
        order.setName(deliveryOrder.getName());
        order.setNumber(deliveryOrder.getNumber());
        order.setPhone(deliveryOrder.getPhone());
        order.setNumber(deliveryOrder.getNumber());
        order.setPrice(deliveryOrder.getPrice());
        order.setCompany(deliveryOrder.getCompany());
        order.setAddress(deliveryOrder.getAddress());
        order.setRemarks(deliveryOrder.getRemarks());
        order.setUsername(StringEncryptUtils.encryptString(deliveryOrder.getUsername()));
        order.setRemarks(deliveryOrder.getRemarks());
        deliveryMapper.insertDeliveryOrder(order);
    }
}
