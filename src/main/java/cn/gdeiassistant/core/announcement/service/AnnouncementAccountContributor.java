package cn.gdeiassistant.core.announcement.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import org.springframework.stereotype.Component;

/**
 * Announcement read records are removed after commit.
 */
@Component
public class AnnouncementAccountContributor implements AccountLifecycleContributor {

    private final AnnouncementMapper announcementMapper;

    public AnnouncementAccountContributor(AnnouncementMapper announcementMapper) {
        this.announcementMapper = announcementMapper;
    }

    @Override
    public void cleanupAfterCommit(AccountClosure closure) {
        announcementMapper.deleteUserReads(closure.username());
    }
}
