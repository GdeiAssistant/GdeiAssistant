package cn.gdeiassistant.core.announcement.service;

import cn.gdeiassistant.common.pojo.entity.AnnouncementEntity;
import cn.gdeiassistant.common.spi.AnnouncementStore;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementStoreService implements AnnouncementStore {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementStoreService(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public AnnouncementEntity queryLatestAnnouncement() {
        return announcementMapper.queryLatestAnnouncement();
    }

    @Override
    public AnnouncementEntity queryAnnouncementById(Integer id) {
        return announcementMapper.queryAnnouncementById(id);
    }

    @Override
    public List<AnnouncementEntity> queryAnnouncementPage(Integer start, Integer size) {
        return announcementMapper.queryAnnouncementPage(start, size);
    }

    @Override
    public void insertAnnouncement(AnnouncementEntity announcement) {
        announcementMapper.insertAnnouncement(announcement);
    }
}
