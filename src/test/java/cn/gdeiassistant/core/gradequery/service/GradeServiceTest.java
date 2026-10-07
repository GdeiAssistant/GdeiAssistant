package cn.gdeiassistant.core.gradequery.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.queryexception.*;
import cn.gdeiassistant.common.pojo.document.GradeDocument;
import cn.gdeiassistant.common.pojo.entity.*;
import cn.gdeiassistant.core.grade.repository.GradeDao;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {
    @Mock UserCertificateService certificates;
    @Mock GradeDao grades;
    @Mock EduSystemClient upstream;
    @InjectMocks GradeService service;

    @BeforeEach
    void login() {
        lenient()
                .when(certificates.getUserLoginCertificate("session"))
                .thenReturn(new User("owner"));
    }

    Grade grade(String term) {
        Grade g = new Grade();
        g.setGradeTerm(term);
        g.setGradeName("Synthetic course");
        return g;
    }

    GradeDocument cache() {
        GradeDocument d = new GradeDocument();
        d.setGradeList(List.of(List.of(grade("1")), List.of(grade("1"), grade("2"))));
        d.setFirstTermGPAList(List.of(1., 3.));
        d.setSecondTermGPAList(List.of(2., 4.));
        d.setFirstTermIGPList(List.of(3., 6.));
        d.setSecondTermIGPList(List.of(4., 8.));
        return d;
    }

    void remote(String rows) throws Exception {
        UserCertificateEntity entity = new UserCertificateEntity();
        entity.setUser(new User("owner"));
        entity.setNumber("10000000001");
        entity.setTimestamp(1L);
        entity.setKeycode("synthetic");
        when(certificates.getUserSessionCertificate("session")).thenReturn(entity);
        lenient()
                .when(upstream.fetchGradeListPage(eq("session"), any()))
                .thenReturn(
                        Jsoup.parse(
                                "<select"
                                    + " id='ddlXN'><option>2025-2026</option><option></option><option>2024-2025</option></select><input"
                                    + " name='__VIEWSTATE' value='state'>"));
        lenient()
                .when(upstream.fetchGradeByYear(eq("session"), any(), eq("state"), anyString()))
                .thenReturn(
                        Jsoup.parse(
                                "<table class='datelist'><tr><th>header</th></tr>"
                                        + rows
                                        + "</table>"));
    }

    String row(String term, String credit, String gpa) {
        return "<tr><td>2025-2026</td><td>"
                + term
                + "</td><td>id</td><td>Course</td><td>required</td><td></td><td>"
                + credit
                + "</td><td>"
                + gpa
                + "</td><td>90</td></tr>";
    }

    @Test
    void cacheLatestPartitionsTermsAndPreservesMetrics() throws Exception {
        when(grades.queryGrade("owner")).thenReturn(cache());
        var r = service.queryGrade("session", null);
        assertEquals(1, r.getYear());
        assertEquals(3., r.getFirstTermGPA());
        assertEquals(8., r.getSecondTermIGP());
        assertEquals(1, r.getFirstTermGradeList().size());
        assertEquals(1, r.getSecondTermGradeList().size());
        verifyNoInteractions(upstream);
    }

    @Test
    void explicitYearAndOutOfRangeReturnExpectedCacheState() throws Exception {
        when(grades.queryGrade("owner")).thenReturn(cache());
        assertEquals(0, service.queryGrade("session", 0).getYear());
        var r = service.queryGrade("session", 8);
        assertTrue(r.getFirstTermGradeList().isEmpty());
        assertTrue(r.getSecondTermGradeList().isEmpty());
        assertEquals(0., r.getFirstTermIGP());
        assertEquals(0., r.getSecondTermGPA());
    }

    @Test
    void absentCachePayloadIsEmpty() throws Exception {
        when(grades.queryGrade("owner")).thenReturn(new GradeDocument());
        assertNull(service.queryGrade("session", null));
    }

    @Test
    void incompleteCacheMetricsAreRejected() {
        GradeDocument d = cache();
        d.setFirstTermGPAList(null);
        d.setSecondTermGPAList(null);
        d.setFirstTermIGPList(null);
        d.setSecondTermIGPList(null);
        when(grades.queryGrade("owner")).thenReturn(d);
        assertThrows(NotAvailableConditionException.class, () -> service.queryGrade("session", 0));
    }

    @Test
    void invalidNegativeYearDoesNotContactUpstream() {
        assertThrows(NotAvailableConditionException.class, () -> service.queryGrade("session", -2));
        verifyNoInteractions(grades, upstream);
    }

    @Test
    void remoteWeightedGpaAndLatestYear() throws Exception {
        remote(row("1", "2", "4") + row("1", "1", "2") + row("2", "3", "3"));
        var r = service.queryGrade("session", null);
        assertEquals(1, r.getYear());
        assertEquals(3.33, r.getFirstTermGPA());
        assertEquals(10., r.getFirstTermIGP());
        assertEquals(3., r.getSecondTermGPA());
        assertEquals("Course", r.getFirstTermGradeList().get(0).getGradeName());
        verify(upstream).fetchGradeByYear(eq("session"), any(), eq("state"), eq("2025-2026"));
    }

    @Test
    void remoteEmptyTermsHaveZeroGpa() throws Exception {
        remote("");
        var r = service.queryGrade("session", 0);
        assertEquals(0., r.getFirstTermGPA());
        assertEquals(0., r.getSecondTermIGP());
    }

    @Test
    void remoteGpaDoesNotDependOnServerLocale() throws Exception {
        remote(row("1", "2", "3.25"));
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.GERMANY);
            assertEquals(3.25, service.queryGrade("session", 0).getFirstTermGPA());
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    void remoteUnavailableYearIsRejectedBeforeFetchingGrades() throws Exception {
        remote("");
        assertThrows(NotAvailableConditionException.class, () -> service.queryGrade("session", 8));
        verify(upstream, never()).fetchGradeByYear(anyString(), any(), anyString(), anyString());
    }

    @Test
    void malformedRowsProduceDomainError() throws Exception {
        remote("<tr><td>invalid</td></tr>");
        assertThrows(ServerErrorException.class, () -> service.queryGrade("session", 0));
    }

    @Test
    void missingGradeTableProducesDomainError() throws Exception {
        remote("");
        when(upstream.fetchGradeByYear(anyString(), any(), anyString(), anyString()))
                .thenReturn(Jsoup.parse("<p>changed</p>"));
        assertThrows(ServerErrorException.class, () -> service.queryGrade("session", 0));
    }

    @Test
    void upstreamErrorsRemainTyped() throws Exception {
        remote("");
        doThrow(new IOException()).when(upstream).fetchGradeListPage(anyString(), any());
        assertThrows(NetWorkTimeoutException.class, () -> service.queryGrade("session", 0));
        doThrow(new PasswordIncorrectException("synthetic"))
                .when(upstream)
                .fetchGradeListPage(anyString(), any());
        assertThrows(PasswordIncorrectException.class, () -> service.queryGrade("session", 0));
        doThrow(new TimeStampIncorrectException("synthetic"))
                .when(upstream)
                .fetchGradeListPage(anyString(), any());
        assertThrows(TimeStampIncorrectException.class, () -> service.queryGrade("session", 0));
    }

    @Test
    void explicitCacheInvalidationUsesCurrentOwner() {
        service.clearGrade("session");
        verify(grades).removeGrade("owner");
    }

    @Test
    void refreshInvalidatesThenRequeries() throws Exception {
        remote("");
        service.updateGradeCache("session");
        verify(grades).removeGrade("owner");
        verify(upstream).fetchGradeListPage(eq("session"), any());
    }
}
