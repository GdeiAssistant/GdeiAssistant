package cn.gdeiassistant.core.topic.converter;

import cn.gdeiassistant.core.topic.pojo.entity.TopicEntity;
import cn.gdeiassistant.core.topic.pojo.vo.TopicVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TopicConverter {

    TopicVO toVO(TopicEntity entity);

    List<TopicVO> toVOList(List<TopicEntity> entities);
}
