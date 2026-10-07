package cn.gdeiassistant.core.evaluate.service;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.exception.evaluateexception.NotAvailableTimeException;
import cn.gdeiassistant.common.exception.queryexception.TimeStampIncorrectException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.edu.EduSystemClient;
import cn.gdeiassistant.integration.edu.pojo.EduSessionCredential;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import java.io.IOException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluateServiceTest {
    private final EduSystemClient client = mock(EduSystemClient.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final EvaluateService service = new EvaluateService();
    private static final String FORM = """
        <form id='Form1' action='save.aspx'><input name='__VIEWSTATE' value='fresh'>
        <input name='__EVENTTARGET' value='target'><input name='__EVENTARGUMENT' value='argument'>
        <table id='Table2'><tr><td><input name='question' value='q'><select name='quality'></select>
        <select name='pjkc'></select></td></tr></table></form>
        """;
    @BeforeEach void setup() throws Exception {
        ReflectionTestUtils.setField(service, "eduSystemClient", client);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        var certificate = new UserCertificateEntity(); certificate.setUser(new User("owner"));
        certificate.setNumber("number"); certificate.setKeycode("key"); certificate.setTimestamp(123L);
        when(certificates.getUserSessionCertificate("session")).thenReturn(certificate);
    }
    private void ready() throws Exception {
        when(client.fetchEduMainPage(eq("session"), any())).thenReturn(Jsoup.parse("""
          <div class='nav'><div class='top'><span>其他</span></div><div class='top'><span>教学评价</span>
          <ul class='sub'><li><a href='evaluate.aspx?course=first&x=1'>first</a></li>
          <li><a href='evaluate.aspx?course=second&x=1'>second</a></li></ul></div></div>
          """));
        when(client.fetchEduPage(eq("session"), any(), anyString())).thenReturn(Jsoup.parse(FORM));
        when(client.submitSpareRoomForm(eq("session"), any(), eq("save.aspx"), anyList())).thenReturn(Jsoup.parse(FORM));
    }
    @Test void saveOnlyNeverSendsFinalSubmissionAndUsesEachCourse() throws Exception {
        ready(); service.teacherEvaluate("session", false);
        ArgumentCaptor<List<BasicNameValuePair>> forms = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<EduSessionCredential> credential = ArgumentCaptor.forClass(EduSessionCredential.class);
        verify(client, times(2)).submitSpareRoomForm(eq("session"), credential.capture(), eq("save.aspx"), forms.capture());
        assertEquals("owner", credential.getValue().getUsername()); assertEquals("number", credential.getValue().getNumber());
        assertEquals("key", credential.getValue().getKeycode()); assertEquals(123L, credential.getValue().getTimestamp());
        assertEquals("first", value(forms.getAllValues().get(0), "pjkc"));
        assertEquals("second", value(forms.getAllValues().get(1), "pjkc"));
        for (var form : forms.getAllValues()) {
            assertEquals("优秀", value(form, "quality")); assertEquals("fresh", value(form, "__VIEWSTATE"));
            assertEquals("q", value(form, "question")); assertEquals("保  存", value(form, "Button1"));
            assertNull(value(form, "Button2"));
        }
    }
    @Test void directSubmitAddsExactlyOneFinalSubmissionAfterSaving() throws Exception {
        ready(); service.teacherEvaluate("session", true);
        ArgumentCaptor<List<BasicNameValuePair>> forms = ArgumentCaptor.forClass(List.class);
        verify(client, times(3)).submitSpareRoomForm(eq("session"), any(), eq("save.aspx"), forms.capture());
        var last = forms.getAllValues().get(2);
        assertEquals("提  交", value(last, "Button2")); assertNull(value(last, "Button1"));
        assertEquals("second", value(last, "pjkc")); assertEquals("优秀", value(last, "quality"));
    }
    @Test void closedEvaluationAndMissingMenuNeverSubmit() throws Exception {
        when(client.fetchEduMainPage(eq("session"), any())).thenReturn(
                Jsoup.parse("<div class='nav'><div class='top'><span>教学评价</span></div></div>"),
                Jsoup.parse("<div class='nav'><div class='top'><span>教学评价</span><ul class='sub'></ul></div></div>"),
                Jsoup.parse("<div class='nav'><div class='top'><span>其他</span></div></div>"));
        assertThrows(NotAvailableTimeException.class, () -> service.teacherEvaluate("session", true));
        assertThrows(NotAvailableTimeException.class, () -> service.teacherEvaluate("session", true));
        assertThrows(ServerErrorException.class, () -> service.teacherEvaluate("session", true));
        verify(client, never()).submitSpareRoomForm(anyString(), any(), anyString(), anyList());
    }
    @Test void upstreamErrorsRetainTheirMeaning() throws Exception {
        when(client.fetchEduMainPage(eq("session"), any())).thenThrow(new IOException("synthetic"),
                new TimeStampIncorrectException("synthetic"), new PasswordIncorrectException("synthetic"),
                new IllegalStateException("synthetic"));
        assertThrows(NetWorkTimeoutException.class, () -> service.teacherEvaluate("session", false));
        assertThrows(TimeStampIncorrectException.class, () -> service.teacherEvaluate("session", false));
        assertThrows(PasswordIncorrectException.class, () -> service.teacherEvaluate("session", false));
        assertThrows(ServerErrorException.class, () -> service.teacherEvaluate("session", false));
    }
    private static String value(List<BasicNameValuePair> form, String name) {
        return form.stream().filter(p -> p.getName().equals(name)).map(BasicNameValuePair::getValue).findFirst().orElse(null);
    }
}
