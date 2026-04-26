package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.common.pojo.Document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userLogin.service.UserLoginService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleCronServiceTest {

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private CronMapper cronMapper;

    @Mock
    private ScheduleDao scheduleDao;

    @Mock
    private CampusCredentialService campusCredentialService;

    @InjectMocks
    private ScheduleCronService scheduleCronService;

    @Test
    void synchronizeScheduleDataOnlyProcessesUsersAllowedByEffectiveQuickAuth() {
        User activeUser = new User("active-user", "saved-credential");
        User noConsentUser = new User("no-consent-user", "saved-credential");
        User disabledUser = new User("disabled-user", "saved-credential");
        List<User> cacheAllowUsers = List.of(activeUser, noConsentUser, disabledUser);

        ScheduleDocument freshSchedule = new ScheduleDocument();
        freshSchedule.setUsername("active-user");
        freshSchedule.setUpdateDateTime(new Date());

        when(cronMapper.selectCacheAllowUsers()).thenReturn(cacheAllowUsers);
        when(campusCredentialService.filterUsersWithEffectiveQuickAuth(cacheAllowUsers))
                .thenReturn(List.of(activeUser));
        when(scheduleDao.querySchedule("active-user")).thenReturn(freshSchedule);

        scheduleCronService.synchronizeScheduleData();

        verify(cronMapper).selectCacheAllowUsers();
        verify(campusCredentialService).filterUsersWithEffectiveQuickAuth(cacheAllowUsers);
        verify(scheduleDao).querySchedule("active-user");
        verifyNoMoreInteractions(scheduleDao);
    }
}
