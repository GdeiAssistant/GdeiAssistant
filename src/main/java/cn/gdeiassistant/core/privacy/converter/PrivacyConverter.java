package cn.gdeiassistant.core.privacy.converter;

import cn.gdeiassistant.core.privacy.pojo.entity.PrivacyEntity;
import cn.gdeiassistant.core.privacy.pojo.vo.PrivacyVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PrivacyConverter {

    PrivacyVO toVO(PrivacyEntity entity);

    List<PrivacyVO> toVOList(List<PrivacyEntity> entities);
}
