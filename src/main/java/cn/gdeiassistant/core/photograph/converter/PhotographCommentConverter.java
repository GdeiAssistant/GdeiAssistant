package cn.gdeiassistant.core.photograph.converter;

import cn.gdeiassistant.core.photograph.pojo.entity.PhotographCommentEntity;
import cn.gdeiassistant.core.photograph.pojo.vo.PhotographCommentVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PhotographCommentConverter {

    PhotographCommentVO toVO(PhotographCommentEntity entity);

    List<PhotographCommentVO> toVOList(List<PhotographCommentEntity> entities);
}
