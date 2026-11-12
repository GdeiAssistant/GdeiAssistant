package cn.gdeiassistant.core.schedule.service;

import cn.gdeiassistant.common.account.AccountClosure;
import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import org.springframework.stereotype.Component;

/**
 * Schedule cache is derived data from the academic system and is removed after commit.
 */
@Component
public class ScheduleAccountContributor implements AccountLifecycleContributor {

    private final ScheduleDao scheduleDao;

    public ScheduleAccountContributor(ScheduleDao scheduleDao) {
        this.scheduleDao = scheduleDao;
    }

    @Override
    public void cleanupAfterCommit(AccountClosure closure) {
        scheduleDao.removeSchedule(closure.username());
    }
}
