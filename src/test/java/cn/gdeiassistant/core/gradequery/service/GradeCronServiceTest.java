package cn.gdeiassistant.core.gradequery.service;

import cn.gdeiassistant.common.pojo.document.GradeDocument;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.campuscredential.service.CampusCredentialService;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
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
class GradeCronServiceTest {

    @Mock
    private UserLoginService userLoginService;

    @Mock
    private GradeService gradeService;

    @Mock
    private CronMapper cronMapper;

    @Mock
    private GradeDao gradeDao;

    @Mock
    private CampusCredentialService campusCredentialService;

    @InjectMocks
    private GradeCronService gradeCronService;

    @Test
    void synchronizeGradeDataOnlyProcessesUsersAllowedByEffectiveQuickAuth() {
        User activeUser = new User("active-user", "saved-credential");
        User revokedUser = new User("revoked-user", "saved-credential");
        User disabledUser = new User("disabled-user", "saved-credential");
        List<User> cacheAllowUsers = List.of(activeUser, revokedUser, disabledUser);

        GradeDocument freshGrade = new GradeDocument();
        freshGrade.setUsername("active-user");
        freshGrade.setUpdateDateTime(new Date());

        when(cronMapper.selectCacheAllowUsers()).thenReturn(cacheAllowUsers);
        when(campusCredentialService.filterUsersWithEffectiveQuickAuth(cacheAllowUsers))
                .thenReturn(List.of(activeUser));
        when(gradeDao.queryGrade("active-user")).thenReturn(freshGrade);

        gradeCronService.synchronizeGradeData();

        verify(cronMapper).selectCacheAllowUsers();
        verify(campusCredentialService).filterUsersWithEffectiveQuickAuth(cacheAllowUsers);
        verify(gradeDao).queryGrade("active-user");
        verifyNoMoreInteractions(gradeDao);
    }

    @Test void expiredCachesRetainIdsAndOnlySuccessfulYearsAreSavedInBatches() throws Exception {
        org.springframework.test.util.ReflectionTestUtils.setField(gradeCronService, "campusSyncExecutor", (java.util.concurrent.Executor) Runnable::run);
        var users = java.util.stream.IntStream.range(0, 6).mapToObj(i -> new User("synthetic-" + i, "synthetic-password")).toList();
        when(cronMapper.selectCacheAllowUsers()).thenReturn(users);
        when(campusCredentialService.filterUsersWithEffectiveQuickAuth(users)).thenReturn(users);
        GradeDocument old = new GradeDocument(); old.setId("existing-id");
        old.setUpdateDateTime(new Date(System.currentTimeMillis() - java.time.Duration.ofDays(8).toMillis()));
        when(gradeDao.queryGrade("synthetic-0")).thenReturn(old);
        when(gradeService.queryGrade(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyInt())).thenAnswer(a -> {
            int year = a.getArgument(1); if (year == 3) return null;
            var result = new cn.gdeiassistant.core.gradequery.pojo.GradeQueryResult(); result.setYear(year);
            result.setFirstTermGPA(3.1 + year); result.setSecondTermGPA(3.2 + year);
            result.setFirstTermIGP(30.1 + year); result.setSecondTermIGP(30.2 + year);
            var first = new cn.gdeiassistant.common.pojo.entity.Grade(); first.setGradeName("first-" + year);
            var second = new cn.gdeiassistant.common.pojo.entity.Grade(); second.setGradeName("second-" + year);
            result.setFirstTermGradeList(List.of(first)); result.setSecondTermGradeList(List.of(second)); return result;
        });
        gradeCronService.synchronizeGradeData();
        var saved = org.mockito.ArgumentCaptor.forClass(GradeDocument.class);
        org.mockito.Mockito.verify(gradeDao, org.mockito.Mockito.times(6)).saveGrade(saved.capture());
        var first = saved.getAllValues().get(0); org.junit.jupiter.api.Assertions.assertEquals("existing-id", first.getId());
        org.junit.jupiter.api.Assertions.assertEquals("synthetic-0", first.getUsername());
        org.junit.jupiter.api.Assertions.assertEquals(List.of(3.1,4.1,5.1), first.getFirstTermGPAList());
        org.junit.jupiter.api.Assertions.assertEquals(List.of(3.2,4.2,5.2), first.getSecondTermGPAList());
        org.junit.jupiter.api.Assertions.assertEquals(3, first.getGradeList().size());
        org.junit.jupiter.api.Assertions.assertEquals("second-0", first.getGradeList().get(0).get(1).getGradeName());
        org.junit.jupiter.api.Assertions.assertNotNull(first.getUpdateDateTime());
    }
    @Test void failureAndInterruptionNeverLeakSemaphorePermits() throws Exception {
        var semaphore = new java.util.concurrent.Semaphore(1);
        org.mockito.Mockito.doThrow(new cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException("synthetic"))
                .when(userLoginService).userLogin(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString());
        var result = gradeCronService.asyncQueryGrade(semaphore, new User("owner", "synthetic")).join();
        org.junit.jupiter.api.Assertions.assertEquals(1, semaphore.availablePermits());
        org.junit.jupiter.api.Assertions.assertNull(result.getGradeListArray()[0]);
        Thread.currentThread().interrupt();
        try { org.junit.jupiter.api.Assertions.assertNull(gradeCronService.asyncQueryGrade(semaphore, new User("owner")).join()); }
        finally { Thread.interrupted(); }
        org.junit.jupiter.api.Assertions.assertEquals(1, semaphore.availablePermits());
    }
}
