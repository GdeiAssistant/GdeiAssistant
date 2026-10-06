package cn.gdeiassistant.core.schedulequery.service;

import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ScheduleParsingBoundsTest {
    @Test void malformedTeacherCourseFailsAsAnUpstreamFormatError() throws Exception {
        ScheduleService service = new ScheduleService();
        EduSystemClient edu = mock(EduSystemClient.class);
        ReflectionTestUtils.setField(service, "eduSystemClient", edu);
        when(edu.fetchTeacherScheduleDocument(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(Jsoup.parse("<table id='Table6'><tr></tr><tr></tr><tr><td>day</td><td>lesson</td><td>incomplete</td></tr></table>"));
        assertThrows(ServerErrorException.class, () -> service.teacherScheduleQuery("synthetic", "teacher", "unused", "2026", "1", "Teacher"));
    }
}
