package cn.gdeiassistant.core.lostandfound.converter;

import cn.gdeiassistant.core.profile.converter.UserProfileMapper;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundDetailEntity;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = { LostAndFoundItemConverter.class, UserProfileMapper.class }, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface LostAndFoundDetailConverter {

    LostAndFoundDetailVO toVO(LostAndFoundDetailEntity entity);
}
