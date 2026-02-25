package cn.gdeiassistant.core.lostandfound.converter;

import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LostAndFoundItemConverter {

    LostAndFoundItemVO toVO(LostAndFoundItemEntity entity);

    List<LostAndFoundItemVO> toVOList(List<LostAndFoundItemEntity> entities);
}
