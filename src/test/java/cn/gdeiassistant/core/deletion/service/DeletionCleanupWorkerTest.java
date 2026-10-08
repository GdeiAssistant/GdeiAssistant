package cn.gdeiassistant.core.deletion.service;

import cn.gdeiassistant.core.deletion.mapper.DeletionCleanupMapper;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.core.close.mapper.CloseMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DeletionCleanupWorkerTest {
    @InjectMocks DeletionCleanupWorker worker;
    @Mock DeletionCleanupMapper mapper;
    @Mock GradeDao gradeDao;
    @Mock ScheduleDao scheduleDao;
    @Mock UserProfileService profileService;
    @Mock UserCertificateService certificateService;
    @Mock CloseMapper closeMapper;
    @Mock cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper announcements;
    @Mock cn.gdeiassistant.core.user.mapper.UserMapper userMapper;
    @Mock org.springframework.transaction.PlatformTransactionManager transactions;
    @org.junit.jupiter.api.BeforeEach void transaction() {
        when(transactions.getTransaction(any())).thenReturn(new org.springframework.transaction.support.SimpleTransactionStatus());
    }

    private void claimed() {
        var task = new DeletionCleanupMapper.CleanupTask();
        task.setUsername("synthetic"); task.setResetname("deleted-synthetic");
        when(mapper.claim("task")).thenReturn(1);
        when(mapper.find("task")).thenReturn(task);
    }
    @Test void externalFailureRemainsRetryableWithoutStoringExceptionText() {
        claimed();
        doThrow(new IllegalStateException("sensitive external response")).when(gradeDao).removeGrade("synthetic");
        worker.runOne("task");
        verify(certificateService).clearReusableCredentials("synthetic");
        verify(mapper).retry("task", "IllegalStateException");
        verify(mapper, never()).complete(anyString());
        verifyNoInteractions(profileService, closeMapper);
    }
    @Test void replayCompletesEveryStoreAfterEarlierFailure() {
        claimed(); worker.runOne("task");
        verify(gradeDao).removeGrade("synthetic"); verify(scheduleDao).removeSchedule("synthetic");
        verify(profileService).deleteAvatarForUsername("synthetic"); verify(closeMapper).insertCloseLog(any());
        verify(announcements).deleteUserReads("synthetic");
        verify(mapper).complete("task"); verify(mapper, never()).retry(anyString(), anyString());
    }
    @Test void unavailableQueueCannotTurnCommittedDeletionIntoAnHttpFailure() {
        when(mapper.claim("task")).thenThrow(new IllegalStateException("private connection details"));
        assertDoesNotThrow(() -> worker.afterCommit(new DeletionCleanupWorker.CleanupRequested("task")));
        verifyNoInteractions(certificateService, gradeDao, scheduleDao, profileService, closeMapper);
    }
}
