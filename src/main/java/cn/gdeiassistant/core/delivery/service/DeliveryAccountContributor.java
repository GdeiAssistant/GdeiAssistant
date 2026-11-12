package cn.gdeiassistant.core.delivery.service;

import cn.gdeiassistant.common.account.AccountCloseBlocker;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import cn.gdeiassistant.common.account.AccountDataExport;
import cn.gdeiassistant.common.constant.ItemConstantUtils;

/**
 * Campus delivery: orders nobody accepted and accepted orders with a pending trade block closure.
 */
@Component
public class DeliveryAccountContributor implements AccountLifecycleContributor {

    /** Index into DELIVERY_ORDER_STATE_TYPE: not accepted. */
    private static final int ORDER_OPEN = 0;
    /** Index into DELIVERY_ORDER_STATE_TYPE: accepted. */
    private static final int ORDER_ACCEPTED = 1;
    /** Index into DELIVERY_TRADE_STATE_TYPE: waiting for completion. */
    private static final int TRADE_PENDING = 0;

    private final DeliveryMapper deliveryMapper;

    public DeliveryAccountContributor(DeliveryMapper deliveryMapper) {
        this.deliveryMapper = deliveryMapper;
    }

    @Override
    public List<AccountCloseBlocker> blockers(String username) {
        List<AccountCloseBlocker> blockers = new ArrayList<>();
        for (DeliveryOrderEntity order : deliveryMapper.selectDeliveryOrderByUsername(username)) {
            if (order.getState() == null) {
                continue;
            }
            if (order.getState() == ORDER_OPEN) {
                blockers.add(new AccountCloseBlocker(
                        "全民快递平台存在未删除的订单信息", "请删除账号下的所有快递代收订单信息"));
            } else if (order.getState() == ORDER_ACCEPTED) {
                DeliveryTradeEntity trade = deliveryMapper.selectDeliveryTradeByOrderId(order.getOrderId());
                if (trade != null && trade.getState() != null && trade.getState() == TRADE_PENDING) {
                    blockers.add(new AccountCloseBlocker(
                            "全民快递平台存在未确认交易的订单信息", "请删除账号下的所有快递代收订单信息"));
                }
            }
        }
        return blockers;
    }

    @Override
    public void decorateExport(AccountDataExport export) {
        export.relabel("deliveryOrders", "state", ItemConstantUtils.DELIVERY_ORDER_STATE_TYPE);
        export.relabel("deliveryTrades", "state", ItemConstantUtils.DELIVERY_TRADE_STATE_TYPE);
    }
}
