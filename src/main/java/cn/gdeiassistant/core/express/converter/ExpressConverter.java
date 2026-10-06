package cn.gdeiassistant.core.express.converter;

import cn.gdeiassistant.core.express.pojo.entity.ExpressEntity;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ExpressConverter {

    @Mapping(target = "username", expression = "java(null)")
    @Mapping(target = "realname", expression = "java(null)")
    @Mapping(target = "canGuess",
            expression = "java(cn.gdeiassistant.common.tools.utils.StringUtils.isNotBlank(entity.getRealname()))")
    ExpressVO toVO(ExpressEntity entity);

    List<ExpressVO> toVOList(List<ExpressEntity> entities);
}
