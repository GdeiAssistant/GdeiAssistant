package cn.gdeiassistant.core.information.service.Announcement;

import cn.gdeiassistant.common.pojo.Entity.Announcement;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import cn.gdeiassistant.core.information.converter.AnnouncementConverter;
import cn.gdeiassistant.core.information.pojo.entity.AnnouncementEntity;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

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
}
