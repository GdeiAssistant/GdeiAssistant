package cn.gdeiassistant.core.profile.converter;

import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserProfileMapper {

    /**
     * Entity -> VO 映射，avatarURL 由业务单独填充，这里忽略
     */
    @Mapping(target = "avatarURL", ignore = true)
    ProfileVO toVO(ProfileEntity entity);
}

