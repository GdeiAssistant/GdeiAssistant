package cn.gdeiassistant.core.lostandfound.converter;

import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LostAndFoundItemConverter {

    @Mapping(target = "authorId", ignore = true)
    LostAndFoundItemVO toVO(LostAndFoundItemEntity entity);

    List<LostAndFoundItemVO> toVOList(List<LostAndFoundItemEntity> entities);
}
