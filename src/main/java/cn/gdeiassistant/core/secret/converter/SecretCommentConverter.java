package cn.gdeiassistant.core.secret.converter;

import cn.gdeiassistant.core.secret.pojo.entity.SecretCommentEntity;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SecretCommentConverter {

    @Mapping(target = "username",
            expression = "java(cn.gdeiassistant.common.tools.utils.AnonymizeUtils.treeholeAnonymousLabel())")
    SecretCommentVO toVO(SecretCommentEntity entity);

    List<SecretCommentVO> toVOList(List<SecretCommentEntity> entities);
}
