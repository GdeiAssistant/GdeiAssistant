package cn.gdeiassistant.core.spareroom.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.queryexception.*;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.spareroom.pojo.dto.EmptyClassroomQueryDTO;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;

import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

@ExtendWith(MockitoExtension.class)
class SpareRoomServiceTest {
    @Mock UserCertificateService certificates;
    @Mock EduSystemClient upstream;
    @InjectMocks SpareRoomService service;

    @BeforeEach
    void setup() throws Exception {
        UserCertificateEntity e = new UserCertificateEntity();
        e.setUser(new User("owner"));
        e.setNumber("10000000001");
        e.setKeycode("synthetic");
        e.setTimestamp(1L);
        when(certificates.getUserSessionCertificate("session")).thenReturn(e);
    }

    String form() {
        StringBuilder b =
                new StringBuilder(
                        "<form name='Form1' action='query.aspx'><input name='__EVENTTARGET'"
                            + " value='event'><input name='__EVENTARGUMENT' value='argument'><input"
                            + " name='__VIEWSTATE' value='state'>");
        for (String s : List.of("xiaoq", "jslb", "sjd"))
            b.append("<select id='").append(s).append("'><option value='0'>0</option></select>");
        b.append(
                "<select id='xqj'><option value='1'>1</option><option"
                        + " value='2'>2</option></select><select id='ddlDsz'><option"
                        + " value=''></option><option value='单'>单</option><option"
                        + " value='双'>双</option></select>");
        for (String s : List.of("ddlSyXn", "ddlSyxq", "xn", "xq"))
            b.append("<select id='")
                    .append(s)
                    .append("'><option value='2026' selected='selected'>2026</option></select>");
        return b.append("</form>").toString();
    }

    EmptyClassroomQueryDTO query(int type) {
        EmptyClassroomQueryDTO q = new EmptyClassroomQueryDTO();
        q.setZone(0);
        q.setType(0);
        q.setClassNumber(0);
        q.setStartTime(1);
        q.setEndTime(1);
        q.setMinWeek(0);
        q.setMaxWeek(1);
        q.setWeekType(type);
        return q;
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    void queryMapsFormWeekTypesAndAllCampusZonesAcrossPages(int weekType) throws Exception {
        when(upstream.fetchSpareRoomInitialDocument(eq("session"), any()))
                .thenReturn(Jsoup.parse(form()));
        StringBuilder rows = new StringBuilder();
        for (int zone : new int[] {1, 4, 8, 9, 0})
            rows.append("<tr><td>R")
                    .append(zone)
                    .append("</td><td>Room</td><td>Lecture</td><td>")
                    .append(zone)
                    .append("</td><td>40</td><td>1</td><td>30</td></tr>");
        when(upstream.submitSpareRoomForm(eq("session"), any(), eq("query.aspx"), anyList()))
                .thenReturn(
                        Jsoup.parse(
                                form()
                                        + "<span"
                                        + " id='dpDataGrid1_lblTotalPages'>2</span><table><tr><th>header</th></tr>"
                                        + rows
                                        + "</table>"));
        var r = service.querySpareRoom("session", query(weekType));
        assertEquals(10, r.size());
        assertEquals(
                List.of("海珠", "花都", "广东轻工南海校区", "业余函授校区", "其他"),
                r.subList(0, 5).stream().map(x -> x.getZone()).toList());
        assertEquals("40", r.get(0).getClassSeating());
        assertEquals("30", r.get(0).getExamSeating());
        ArgumentCaptor<List<BasicNameValuePair>> c = ArgumentCaptor.forClass(List.class);
        verify(upstream, times(2))
                .submitSpareRoomForm(eq("session"), any(), eq("query.aspx"), c.capture());
        Map<String, String> first = new HashMap<>();
        c.getAllValues().get(0).forEach(p -> first.put(p.getName(), p.getValue()));
        assertEquals(weekType == 0 ? "" : weekType == 1 ? "单" : "双", first.get("ddlDsz"));
        assertEquals("0", first.get("min_zws"));
        assertEquals("2", first.get("ddlXqm"));
        assertTrue(
                c.getAllValues().get(1).stream()
                        .anyMatch(p -> p.getName().equals("dpDataGrid1:btnNextPage")));
    }

    @Test
    void seatingAndDifferentTimeRangeArePreserved() throws Exception {
        when(upstream.fetchSpareRoomInitialDocument(anyString(), any()))
                .thenReturn(Jsoup.parse(form()));
        when(upstream.submitSpareRoomForm(anyString(), any(), anyString(), anyList()))
                .thenReturn(Jsoup.parse("<table><tr><th>header</th></tr></table>"));
        EmptyClassroomQueryDTO q = query(0);
        q.setMinSeating(20);
        q.setMaxSeating(40);
        q.setEndTime(2);
        assertNull(service.querySpareRoom("session", q));
        ArgumentCaptor<List<BasicNameValuePair>> c = ArgumentCaptor.forClass(List.class);
        verify(upstream).submitSpareRoomForm(eq("session"), any(), anyString(), c.capture());
        assertTrue(
                c.getValue().stream()
                        .anyMatch(p -> p.getName().equals("max_zws") && p.getValue().equals("40")));
        assertFalse(c.getValue().stream().anyMatch(p -> p.getName().equals("ddlXqm")));
    }

    @Test
    void invalidWeekRangeAndMalformedResponseAreTypedErrors() throws Exception {
        when(upstream.fetchSpareRoomInitialDocument(anyString(), any()))
                .thenReturn(Jsoup.parse(form()));
        EmptyClassroomQueryDTO q = query(0);
        q.setMinWeek(6);
        assertThrows(
                ErrorQueryConditionException.class, () -> service.querySpareRoom("session", q));
        when(upstream.fetchSpareRoomInitialDocument(anyString(), any()))
                .thenReturn(Jsoup.parse(""));
        assertThrows(ServerErrorException.class, () -> service.querySpareRoom("session", query(0)));
    }

    @Test
    void upstreamErrorsRemainTyped() throws Exception {
        doThrow(new java.io.IOException())
                .when(upstream)
                .fetchSpareRoomInitialDocument(anyString(), any());
        assertThrows(
                NetWorkTimeoutException.class, () -> service.querySpareRoom("session", query(0)));
        doThrow(new TimeStampIncorrectException("synthetic"))
                .when(upstream)
                .fetchSpareRoomInitialDocument(anyString(), any());
        assertThrows(
                TimeStampIncorrectException.class,
                () -> service.querySpareRoom("session", query(0)));
        doThrow(new PasswordIncorrectException("synthetic"))
                .when(upstream)
                .fetchSpareRoomInitialDocument(anyString(), any());
        assertThrows(
                PasswordIncorrectException.class,
                () -> service.querySpareRoom("session", query(0)));
    }
}
