package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.common.pojo.document.ScheduleDocument;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userlogin.service.UserLoginService;
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

    @Test void staleCacheRefreshRetainsIdentityAndNewCachesAreCreated() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(scheduleCronService, "campusSyncExecutor", (java.util.concurrent.Executor) Runnable::run);
        var users = java.util.stream.IntStream.range(0, 6).mapToObj(i -> new User("synthetic-" + i, "synthetic-password")).toList();
        when(cronMapper.selectCacheAllowUsers()).thenReturn(users);
        when(campusCredentialService.filterUsersWithEffectiveQuickAuth(users)).thenReturn(users);
        ScheduleDocument old = new ScheduleDocument(); old.setId("existing-id");
        old.setUpdateDateTime(new Date(System.currentTimeMillis() - java.time.Duration.ofDays(4).toMillis()));
        when(scheduleDao.querySchedule("synthetic-0")).thenReturn(old);
        var item = new cn.gdeiassistant.common.pojo.entity.Schedule(); item.setScheduleName("合成课程");
        var result = new cn.gdeiassistant.core.schedulequery.pojo.ScheduleQueryResult(List.of(item), 2);
        when(scheduleService.querySchedule(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.eq(0))).thenReturn(result);
        scheduleCronService.synchronizeScheduleData();
        var saved = org.mockito.ArgumentCaptor.forClass(ScheduleDocument.class);
        org.mockito.Mockito.verify(scheduleDao, org.mockito.Mockito.times(6)).saveSchedule(saved.capture());
        var first = saved.getAllValues().get(0);
        org.junit.jupiter.api.Assertions.assertEquals("existing-id", first.getId());
        org.junit.jupiter.api.Assertions.assertEquals("synthetic-0", first.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals("合成课程", first.getScheduleList().get(0).getScheduleName());
        org.junit.jupiter.api.Assertions.assertNotNull(first.getUpdateDateTime());
    }
    @Test void failedLoginAndInterruptedAcquireCannotSaveOrLeakPermits() throws Exception {
        var semaphore = new java.util.concurrent.Semaphore(1);
        org.mockito.Mockito.doThrow(new cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException("synthetic"))
                .when(userLoginService).userLogin(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        org.junit.jupiter.api.Assertions.assertNull(scheduleCronService.asyncQuerySchedule(semaphore, new User("owner", "synthetic")).join());
        org.junit.jupiter.api.Assertions.assertEquals(1, semaphore.availablePermits());
        Thread.currentThread().interrupt();
        try { org.junit.jupiter.api.Assertions.assertNull(scheduleCronService.asyncQuerySchedule(semaphore, new User("owner")).join()); }
        finally { Thread.interrupted(); }
        org.junit.jupiter.api.Assertions.assertEquals(1, semaphore.availablePermits());
        org.mockito.Mockito.verifyNoInteractions(scheduleDao);
    }
}
