package cn.gdeiassistant.core.cron.controller;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.gradequery.service.GradeCronService;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsCornService;
import cn.gdeiassistant.core.schedulequery.service.ScheduleCronService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CronControllerTest {

    @Mock
    private GradeCronService gradeCronService;

    @Mock
    private ScheduleCronService scheduleCronService;

    @Mock
    private SchoolNewsCornService schoolNewsCornService;

    @InjectMocks
    private CronController controller;

    @Test
    void cacheGradeDataRejectsMissingSecret() throws Exception {
        ReflectionTestUtils.setField(controller, "cronSecret", "expected-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonResult result = controller.cacheGradeData(null, response);

        assertNull(result);
        assertEquals(403, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertTrue(response.getContentAsString().contains("X-Cron-Secret"));
        verifyNoInteractions(gradeCronService, scheduleCronService, schoolNewsCornService);
    }

    @Test
    void cacheGradeDataRejectsWrongSecret() throws Exception {
        ReflectionTestUtils.setField(controller, "cronSecret", "expected-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonResult result = controller.cacheGradeData("wrong-secret", response);

        assertNull(result);
        assertEquals(403, response.getStatus());
        verifyNoInteractions(gradeCronService, scheduleCronService, schoolNewsCornService);
    }

    @Test
    void cacheGradeDataRunsWhenSecretMatches() throws Exception {
        ReflectionTestUtils.setField(controller, "cronSecret", "expected-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonResult result = controller.cacheGradeData("expected-secret", response);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(gradeCronService).synchronizeGradeData();
        verifyNoInteractions(scheduleCronService, schoolNewsCornService);
    }

    @Test
    void cacheScheduleDataRunsWhenSecretMatches() throws Exception {
        ReflectionTestUtils.setField(controller, "cronSecret", "expected-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonResult result = controller.cacheScheduleData("expected-secret", response);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(scheduleCronService).synchronizeScheduleData();
        verifyNoInteractions(gradeCronService, schoolNewsCornService);
    }

    @Test
    void collectNewsRunsWhenSecretMatches() throws Exception {
        ReflectionTestUtils.setField(controller, "cronSecret", "expected-secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        JsonResult result = controller.collectNews("expected-secret", response);

        assertNotNull(result);
        assertTrue(result.isSuccess());
        verify(schoolNewsCornService).collectNews();
        verifyNoInteractions(gradeCronService, scheduleCronService);
    }
}
