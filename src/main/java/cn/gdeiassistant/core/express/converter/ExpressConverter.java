package cn.gdeiassistant.core.express.converter;

import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpressConverter {

    ExpressVO toVO(ExpressEntity entity);

    List<ExpressVO> toVOList(List<ExpressEntity> entities);
}
