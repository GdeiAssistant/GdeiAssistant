package cn.gdeiassistant.common.spi;

import cn.gdeiassistant.common.pojo.entity.AnnouncementEntity;

import java.util.List;

/**
 * Contract used by the information module to query and store announcements.
 * The announcement domain owns the implementation.
 */
public interface AnnouncementStore {

    AnnouncementEntity queryLatestAnnouncement();

    AnnouncementEntity queryAnnouncementById(Integer id);

    List<AnnouncementEntity> queryAnnouncementPage(Integer start, Integer size);

    void insertAnnouncement(AnnouncementEntity announcement);
}
