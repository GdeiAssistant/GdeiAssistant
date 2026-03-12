package cn.gdeiassistant.core.message.service.provider;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.delivery.mapper.DeliveryMapper;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.message.pojo.vo.InteractionMessageVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DeliveryInteractionMessageProvider extends BaseInteractionMessageProvider {

    @Resource(name = "deliveryMapper")
    private DeliveryMapper deliveryMapper;

    @Override
    public List<InteractionMessageRecord> queryMessages(String username, int limit) {
        if (StringUtils.isBlank(username) || limit <= 0) {
            return new ArrayList<>();
        }
        List<DeliveryTradeEntity> entityList = deliveryMapper.selectPersonalDeliveryInteractionPage(username, 0, limit);
        List<InteractionMessageRecord> records = new ArrayList<>();
        if (entityList != null) {
            for (DeliveryTradeEntity entity : entityList) {
                records.add(buildRecord(toInteractionMessage(entity), entity.getCreateTime(), entity.getTradeId()));
            }
        }
        return records;
    }

    private InteractionMessageVO toInteractionMessage(DeliveryTradeEntity entity) {
        InteractionMessageVO vo = new InteractionMessageVO();
        DeliveryOrderEntity order = entity != null ? entity.getDeliveryOrder() : null;
        String runner = entity != null && StringUtils.isNotBlank(entity.getUsername()) ? entity.getUsername() : "有同学";
        String company = order != null && StringUtils.isNotBlank(order.getCompany()) ? order.getCompany() : "快递代收";
        vo.setId(entity != null && entity.getTradeId() != null ? "delivery-" + entity.getTradeId() : null);
        vo.setModule("delivery");
        vo.setType("order_accepted");
        vo.setTitle("订单已被接单");
        vo.setContent(runner + " 接取了你发布的 " + company + " 订单");
        vo.setCreatedAt(formatDate(entity != null ? entity.getCreateTime() : null));
        vo.setIsRead(true);
        vo.setTargetType("published");
        vo.setTargetId(toStringValue(order != null ? order.getOrderId() : entity != null ? entity.getOrderId() : null));
        vo.setTargetSubId(toStringValue(entity != null ? entity.getTradeId() : null));
        return vo;
    }
}
