package cn.gdeiassistant.core.delivery.converter;

import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryOrderEntity;
import cn.gdeiassistant.core.delivery.pojo.entity.DeliveryTradeEntity;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryOrderVO;
import cn.gdeiassistant.core.delivery.pojo.vo.DeliveryTradeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryConverter {

    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    DeliveryOrderVO toOrderVO(DeliveryOrderEntity entity);

    List<DeliveryOrderVO> toOrderVOList(List<DeliveryOrderEntity> entities);

    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "displayName", ignore = true)
    DeliveryTradeVO toTradeVO(DeliveryTradeEntity entity);

    List<DeliveryTradeVO> toTradeVOList(List<DeliveryTradeEntity> entities);
}
