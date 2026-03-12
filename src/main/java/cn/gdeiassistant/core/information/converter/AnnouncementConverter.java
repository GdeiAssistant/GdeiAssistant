package cn.gdeiassistant.core.information.converter;

import cn.gdeiassistant.core.information.pojo.entity.AnnouncementEntity;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnnouncementConverter {

    AnnouncementVO toVO(AnnouncementEntity entity);

    List<AnnouncementVO> toVOList(List<AnnouncementEntity> entityList);
}
