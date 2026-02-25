package cn.gdeiassistant.core.secret.converter;

import cn.gdeiassistant.core.secret.pojo.entity.SecretContentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = SecretCommentConverter.class)
public interface SecretConverter {

    /** 基础字段映射；commentCount、likeCount、liked、voiceURL 由 Service 按需覆盖 */
    @Mapping(target = "liked", ignore = true)
    @Mapping(target = "voiceURL", ignore = true)
    SecretVO toVO(SecretContentEntity entity);

    List<SecretVO> toVOList(List<SecretContentEntity> entities);
}
