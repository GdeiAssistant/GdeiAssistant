package cn.gdeiassistant.core.information.converter;

import cn.gdeiassistant.core.information.pojo.entity.ReadingEntity;
import cn.gdeiassistant.core.information.pojo.vo.ReadingVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReadingConverter {

    ReadingVO toVO(ReadingEntity entity);

    List<ReadingVO> toVOList(List<ReadingEntity> entities);
}
