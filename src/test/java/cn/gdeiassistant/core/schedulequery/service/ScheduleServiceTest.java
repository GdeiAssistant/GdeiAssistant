package cn.gdeiassistant.core.schedulequery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.queryexception.*;
import cn.gdeiassistant.common.pojo.document.*;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.core.schedule.repository.ScheduleDao;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {
    @Mock UserCertificateService certificates;
    @Mock ScheduleDao schedules;
    @Mock EduSystemClient upstream;
    @InjectMocks ScheduleService service;

    @BeforeEach
    void setup() {
        cn.gdeiassistant.common.tools.utils.WeekUtils w =
                new cn.gdeiassistant.common.tools.utils.WeekUtils();
        w.setStartYear(2026);
        w.setStartMonth(9);
        w.setStartDate(1);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("owner"));
    }

    Schedule course(int position, int start, int end) {
        Schedule s = new Schedule();
        s.setId("course" + position);
        s.setPosition(position);
        s.setScheduleName("Synthetic course");
        s.setMinScheduleWeek(start);
        s.setMaxScheduleWeek(end);
        s.setScheduleLength(1);
        return s;
    }

    @Test
    void customMergeDoesNotMutateCachedListOrDuplicateOnRepeatedQuery() throws Exception {
        ScheduleDocument d = new ScheduleDocument();
        d.setScheduleList(new ArrayList<>(List.of(course(0, 1, 20))));
        when(schedules.querySchedule("owner")).thenReturn(d);
        CustomScheduleDocument custom = new CustomScheduleDocument();
        custom.setScheduleMap(Map.of("custom", course(7, 2, 3)));
        when(schedules.queryCustomSchedule("owner")).thenReturn(custom);
        assertEquals(2, service.querySchedule("session", 0).getScheduleList().size());
        assertEquals(2, service.querySchedule("session", 0).getScheduleList().size());
        assertEquals(1, d.getScheduleList().size());
        assertEquals(1, service.querySchedule("session", 4).getScheduleList().size());
    }

    @Test
    void emptyCacheAndCurrentWeekAreHandled() throws Exception {
        when(schedules.querySchedule("owner")).thenReturn(new ScheduleDocument());
        assertTrue(service.querySchedule("session", null).getScheduleList().isEmpty());
        assertNotNull(service.querySchedule("session", null).getWeek());
        verifyNoInteractions(upstream);
    }

    void remote(String cell) throws Exception {
        UserCertificateEntity e = new UserCertificateEntity();
        e.setUser(new User("owner"));
        e.setNumber("10000000001");
        e.setTimestamp(1L);
        when(certificates.getUserSessionCertificate("session")).thenReturn(e);
        String row =
                "<tr><td>Morning</td><td>1</td>"
                        + cell
                        + "<td></td><td></td><td></td><td></td><td></td><td></td></tr>";
        when(upstream.fetchScheduleDocument(eq("session"), any()))
                .thenReturn(
                        Jsoup.parse("<table id='Table1'><tr></tr><tr></tr>" + row + "</table>"));
    }

    @Test
    void remoteCourseParsingPreservesTimetableFieldsAndRowspan() throws Exception {
        remote(
                "<td rowspan='2'>Course$info$Required$info$周一第1节{第1-16周}$info$Teacher$info$Room（备注）</td>");
        var r = service.querySchedule("session", 1);
        assertEquals(1, r.getScheduleList().size());
        Schedule s = r.getScheduleList().get(0);
        assertEquals("Course", s.getScheduleName());
        assertEquals("Required", s.getScheduleType());
        assertEquals("Teacher", s.getScheduleTeacher());
        assertEquals("Room", s.getScheduleLocation());
        assertEquals(2, s.getScheduleLength());
        assertEquals(1, s.getMinScheduleWeek());
        assertEquals(16, s.getMaxScheduleWeek());
        assertEquals(0, s.getRow());
        assertEquals(0, s.getColumn());
    }

    @Test
    void remoteCourseWithoutLocationHasExplicitPlaceholder() throws Exception {
        remote("<td>Course$info$Required$info$周一第1节{第1-16周}$info$Teacher</td>");
        assertEquals(
                "暂未安排",
                service.querySchedule("session", 0).getScheduleList().get(0).getScheduleLocation());
    }

    @Test
    void malformedRemoteAndNetworkErrorsRemainTyped() throws Exception {
        remote("<td>incomplete</td>");
        assertThrows(ServerErrorException.class, () -> service.querySchedule("session", 0));
        doThrow(new java.io.IOException()).when(upstream).fetchScheduleDocument(anyString(), any());
        assertThrows(NetWorkTimeoutException.class, () -> service.querySchedule("session", 0));
        doThrow(new PasswordIncorrectException("synthetic"))
                .when(upstream)
                .fetchScheduleDocument(anyString(), any());
        assertThrows(PasswordIncorrectException.class, () -> service.querySchedule("session", 0));
        doThrow(new TimeStampIncorrectException("synthetic"))
                .when(upstream)
                .fetchScheduleDocument(anyString(), any());
        assertThrows(TimeStampIncorrectException.class, () -> service.querySchedule("session", 0));
    }

    @Test
    void invalidationAndDeleteAreScopedToCurrentOwner() throws Exception {
        service.clearSchedule("session");
        verify(schedules).removeSchedule("owner");
        when(schedules.deleteCustomSchedule("owner", 1)).thenReturn(true);
        service.deleteCustomSchedule("session", 1);
        assertThrows(DataNotExistException.class, () -> service.deleteCustomSchedule("session", 2));
    }

    @Test
    void refreshInvalidatesAndFetchesRemote() throws Exception {
        remote("<td></td>");
        service.updateScheduleCache("session");
        verify(schedules).removeSchedule("owner");
        verify(upstream).fetchScheduleDocument(eq("session"), any());
    }
}
