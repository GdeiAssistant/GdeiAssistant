package cn.gdeiassistant.core.cetquery.service;

import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.exception.queryexception.ErrorQueryConditionException;
import cn.gdeiassistant.common.pojo.entity.CetNumber;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.core.cet.mapper.CetMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.integration.chsi.ChsiClient;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CetQueryServiceTest {
    private final CetMapper mapper = mock(CetMapper.class);
    private final UserCertificateService certificates = mock(UserCertificateService.class);
    private final ChsiClient client = mock(ChsiClient.class);
    private final CetQueryService service = new CetQueryService();

    @BeforeEach void setup() {
        ReflectionTestUtils.setField(service, "cetMapper", mapper);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        ReflectionTestUtils.setField(service, "chsiClient", client);
        when(certificates.getUserLoginCertificate("session")).thenReturn(new User("synthetic-user"));
    }

    @Test void queryParsesDistinctScoreComponentsAndDefaultsMissingCaptcha() throws Exception {
        var document = Jsoup.parse("<table class='cetTable'>"
                + "<tr><td>合成姓名</td></tr><tr><td>合成学校</td></tr><tr><td>英语四级</td></tr><tr><td>其他</td></tr>"
                + "<tr><td>synthetic-ticket</td></tr><tr><td class='colorRed'>5 20</td></tr>"
                + "<tr><td>听力</td><td>1 70</td></tr><tr><td>阅读</td><td>1 90</td></tr><tr><td>写作翻译</td><td>1 60</td></tr></table>");
        when(client.fetchCetQueryPage("session", "synthetic-ticket", "合成姓名", "")).thenReturn(document);
        var result = service.queryCetScore("session", "synthetic-ticket", "合成姓名", null);
        assertEquals("合成姓名", result.getName()); assertEquals("合成学校", result.getSchool());
        assertEquals("英语四级", result.getType()); assertEquals("synthetic-ticket", result.getAdmissionCard());
        assertEquals("520", result.getTotalScore()); assertEquals("170", result.getListeningScore());
        assertEquals("190", result.getReadingScore()); assertEquals("160", result.getWritingAndTranslatingScore());
    }

    @Test void upstreamErrorsKeepTheirBusinessClassification() throws Exception {
        when(client.fetchCetQueryPage(anyString(), anyString(), anyString(), anyString())).thenReturn(
                Jsoup.parse("<div class='error alignC marginT20'>查询错误</div>"),
                Jsoup.parse("<div class='error alignC'>验证码错误</div>"), Jsoup.parse("<div>维护</div>"));
        assertThrows(PasswordIncorrectException.class, () -> service.queryCetScore("session", "ticket", "name", "1234"));
        assertThrows(ErrorQueryConditionException.class, () -> service.queryCetScore("session", "ticket", "name", "1234"));
        assertThrows(ServerErrorException.class, () -> service.queryCetScore("session", "ticket", "name", "1234"));
        when(client.fetchCetQueryPage(anyString(), anyString(), anyString(), anyString())).thenThrow(new IOException("synthetic timeout"));
        assertThrows(NetWorkTimeoutException.class, () -> service.queryCetScore("session", "ticket", "name", "1234"));
    }

    @Test void captchaTransportTimeoutIsClassifiedWithoutCallingARealProvider() throws Exception {
        when(client.fetchCetCaptchaImageBase64("session")).thenReturn("synthetic-base64");
        assertEquals("synthetic-base64", service.cetIndex("session"));
        when(client.fetchCetCaptchaImageBase64("session")).thenThrow(new IOException("synthetic timeout"));
        assertThrows(NetWorkTimeoutException.class, () -> service.cetIndex("session"));
    }

    @Test void savedTicketUsesTheSessionOwnerAndDoesNotExposeTheCampusUsername() {
        assertNull(service.getCetNumber("session"));
        service.saveCetNumber("session", 123456789012345L, "ignored-name");
        verify(mapper).insertNumber("synthetic-user", 123456789012345L);
        var number = new CetNumber(); number.setNumber(123456789012345L); number.setUsername("synthetic-user");
        when(mapper.selectNumber("synthetic-user")).thenReturn(number);
        assertEquals(123456789012345L, service.getCetNumber("session").getNumber());
        assertNull(service.getCetNumber("session").getName());
        service.saveCetNumber("session", 999999999999999L, "ignored-name");
        verify(mapper).updateNumber("synthetic-user", 999999999999999L);
        number.setNumber(null); assertNull(service.getCetNumber("session"));
    }
}
