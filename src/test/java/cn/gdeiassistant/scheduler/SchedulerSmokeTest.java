package cn.gdeiassistant.scheduler;

import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsCornService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import cn.gdeiassistant.core.secret.service.SecretService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SchedulerSmokeTest {

    @Mock
    private GradeCronService gradeCronService;

    @Mock
    private ScheduleCronService scheduleCronService;

    @Mock
    private SchoolNewsCornService schoolNewsCornService;

    @Mock
    private SecretService secretService;

    @Test
    void productionSchedulersKeepExpectedProfileAndScheduleMetadata() throws NoSuchMethodException {
        assertProfile(GradeScheduler.class, "production");
        assertProfile(NewsScheduler.class, "production");
        assertProfile(ScheduleScheduler.class, "production");

        assertEquals(7200000L, scheduled(GradeScheduler.class, "synchronizeGradeData").fixedDelay());
        assertEquals("0 0 0,9,18 * * ?", scheduled(NewsScheduler.class, "collectNews").cron());
        assertEquals(7200000L, scheduled(ScheduleScheduler.class, "synchronizeScheduleData").fixedDelay());
        assertEquals(300000L, scheduled(SecretScheduler.class, "deleteTimerSecretInfos").fixedDelay());
    }

    @Test
    void schedulersDelegateWhenServicesAreAvailable() throws Exception {
        GradeScheduler gradeScheduler = new GradeScheduler();
        ReflectionTestUtils.setField(gradeScheduler, "gradeCronService", gradeCronService);
        gradeScheduler.synchronizeGradeData();
        verify(gradeCronService).synchronizeGradeData();

        ScheduleScheduler scheduleScheduler = new ScheduleScheduler();
        ReflectionTestUtils.setField(scheduleScheduler, "scheduleCronService", scheduleCronService);
        scheduleScheduler.synchronizeScheduleData();
        verify(scheduleCronService).synchronizeScheduleData();

        NewsScheduler newsScheduler = new NewsScheduler();
        ReflectionTestUtils.setField(newsScheduler, "schoolNewsCornService", schoolNewsCornService);
        newsScheduler.collectNews();
        verify(schoolNewsCornService).collectNews();

        SecretScheduler secretScheduler = new SecretScheduler();
        ReflectionTestUtils.setField(secretScheduler, "secretService", secretService);
        secretScheduler.deleteTimerSecretInfos();
        verify(secretService).deleteTimerSecretInfos();
    }

    @Test
    void schedulersNoOpWhenServicesAreMissing() {
        assertDoesNotThrow(() -> new GradeScheduler().synchronizeGradeData());
        assertDoesNotThrow(() -> new ScheduleScheduler().synchronizeScheduleData());
        assertDoesNotThrow(() -> new NewsScheduler().collectNews());
        assertDoesNotThrow(() -> new SecretScheduler().deleteTimerSecretInfos());
    }

    private static Scheduled scheduled(Class<?> type, String methodName) throws NoSuchMethodException {
        Scheduled scheduled = type.getDeclaredMethod(methodName).getAnnotation(Scheduled.class);
        assertNotNull(scheduled, () -> type.getSimpleName() + "." + methodName + " 缺少 @Scheduled");
        return scheduled;
    }

    private static void assertProfile(Class<?> type, String... expectedProfiles) {
        Profile profile = type.getAnnotation(Profile.class);
        assertNotNull(profile, () -> type.getSimpleName() + " 缺少 @Profile");
        assertArrayEquals(expectedProfiles, profile.value());
    }
}
