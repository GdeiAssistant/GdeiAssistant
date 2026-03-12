package cn.gdeiassistant.core.information.service.Announcement;

import cn.gdeiassistant.common.pojo.Entity.Announcement;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import cn.gdeiassistant.core.information.converter.AnnouncementConverter;
import cn.gdeiassistant.core.information.pojo.entity.AnnouncementEntity;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private AnnouncementConverter announcementConverter;

    /**
     * 保存通知公告信息
     *
     * @param announcement 请求体（仅用 title、content）
     */
    public void saveAnnouncement(Announcement announcement) {
        AnnouncementEntity entity = new AnnouncementEntity();
        entity.setTitle(announcement.getTitle());
        entity.setContent(announcement.getContent());
        entity.setPublishTime(new Date());
        announcementMapper.insertAnnouncement(entity);
    }

    public AnnouncementVO queryLatestAnnouncement() {
        AnnouncementEntity entity = announcementMapper.queryLatestAnnouncement();
        return entity == null ? null : announcementConverter.toVO(entity);
    }

    public AnnouncementVO queryAnnouncementDetail(Integer id) {
        if (id == null) {
            return null;
        }
        AnnouncementEntity entity = announcementMapper.queryAnnouncementById(id);
        return entity == null ? null : announcementConverter.toVO(entity);
    }

    public List<AnnouncementVO> queryAnnouncementPage(Integer start, Integer size) {
        if (start == null || start < 0 || size == null || size <= 0) {
            return new ArrayList<>();
        }
        List<AnnouncementEntity> entityList = announcementMapper.queryAnnouncementPage(start, size);
        return entityList == null ? new ArrayList<>() : announcementConverter.toVOList(entityList);
    }
}
