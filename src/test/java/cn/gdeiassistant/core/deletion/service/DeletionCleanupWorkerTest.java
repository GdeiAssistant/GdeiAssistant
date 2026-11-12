package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.common.account.AccountLifecycleContributor;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import cn.gdeiassistant.core.announcement.service.AnnouncementAccountContributor;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.grade.service.GradeAccountContributor;
import cn.gdeiassistant.core.profile.mapper.ProfileMapper;
import cn.gdeiassistant.core.profile.service.ProfileAccountContributor;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.schedule.service.ScheduleAccountContributor;
import cn.gdeiassistant.core.user.mapper.UserMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeletionCleanupWorkerTest {

    @Mock private DeletionCleanupMapper mapper;
    @Mock private UserMapper userMapper;
    @Mock private PlatformTransactionManager transactions;
    @Mock private UserCertificateService certificateService;
    @Mock private CloseMapper closeMapper;
    @Mock private GradeDao gradeDao;
    @Mock private ScheduleDao scheduleDao;
    @Mock private ProfileMapper profileMapper;
    @Mock private UserProfileService profileService;
    @Mock private AnnouncementMapper announcements;

    private DeletionCleanupWorker worker;

    @BeforeEach
    void setUp() {
        when(transactions.getTransaction(any())).thenReturn(new SimpleTransactionStatus());
        // List order is execution order: the grade cache fails first so later domains must stay untouched.
        List<AccountLifecycleContributor> contributors = List.of(
                new GradeAccountContributor(gradeDao),
                new ScheduleAccountContributor(scheduleDao),
                new ProfileAccountContributor(profileMapper, profileService),
                new AnnouncementAccountContributor(announcements));
        worker = new DeletionCleanupWorker(
                mapper, userMapper, transactions, certificateService, closeMapper, contributors);
    }

    private void claimed() {
        DeletionCleanupMapper.CleanupTask task = new DeletionCleanupMapper.CleanupTask();
        task.setUsername("synthetic");
        task.setResetname("deleted-synthetic");
        when(mapper.claim("task")).thenReturn(1);
        when(mapper.find("task")).thenReturn(task);
    }

    @Test
    void externalFailureRemainsRetryableWithoutStoringExceptionText() {
        claimed();
        doThrow(new IllegalStateException("sensitive external response")).when(gradeDao).removeGrade("synthetic");

        worker.runOne("task");

        verify(certificateService).clearReusableCredentials("synthetic");
        verify(mapper).retry("task", "IllegalStateException");
        verify(mapper, never()).complete(anyString());
        verifyNoInteractions(profileService, closeMapper);
    }

    @Test
    void replayCompletesEveryStoreAfterEarlierFailure() {
        claimed();

        worker.runOne("task");

        verify(gradeDao).removeGrade("synthetic");
        verify(scheduleDao).removeSchedule("synthetic");
        verify(profileService).deleteAvatarForUsername("synthetic");
        verify(announcements).deleteUserReads("synthetic");
        verify(closeMapper).insertCloseLog(any());
        verify(mapper).complete("task");
        verify(mapper, never()).retry(anyString(), anyString());
    }

    @Test
    void unavailableQueueCannotTurnCommittedDeletionIntoAnHttpFailure() {
        when(mapper.claim("task")).thenThrow(new IllegalStateException("private connection details"));

        assertDoesNotThrow(() -> worker.afterCommit(new DeletionCleanupWorker.CleanupRequested("task")));

        verifyNoInteractions(certificateService, gradeDao, scheduleDao, profileService, closeMapper);
    }
}
