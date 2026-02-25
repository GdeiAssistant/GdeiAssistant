package cn.gdeiassistant.core.photograph.converter;

import cn.gdeiassistant.core.photograph.pojo.entity.PhotographEntity;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = PhotographCommentConverter.class)
public interface PhotographConverter {

    @Mapping(target = "firstImageUrl", ignore = true)
    @Mapping(target = "imageUrls", ignore = true)
    PhotographVO toVO(PhotographEntity entity);

    List<PhotographVO> toVOList(List<PhotographEntity> entities);
}
