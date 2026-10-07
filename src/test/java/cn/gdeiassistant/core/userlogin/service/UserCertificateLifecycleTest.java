package cn.gdeiassistant.core.userlogin.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cn.gdeiassistant.common.exception.commonexception.*;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import cn.gdeiassistant.core.userlogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.integration.httpclient.*;

import org.apache.http.ProtocolVersion;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.*;

@ExtendWith(MockitoExtension.class)
class UserCertificateLifecycleTest {
    @Mock UserCertificateDao dao;
    @Mock HttpClientUtils clients;
    @Mock CloseableHttpClient http;
    @InjectMocks UserCertificateService service;

    CloseableHttpResponse response(int status, String html, String location) {
        CloseableHttpResponse r = mock(CloseableHttpResponse.class);
        lenient()
                .when(r.getStatusLine())
                .thenReturn(
                        new BasicStatusLine(
                                new ProtocolVersion("HTTP", 1, 1), status, "synthetic"));
        lenient().when(r.getEntity()).thenReturn(new StringEntity(html, StandardCharsets.UTF_8));
        lenient()
                .when(r.getFirstHeader("Location"))
                .thenReturn(new BasicHeader("Location", location));
        return r;
    }

    void responses(CloseableHttpResponse... r) throws Exception {
        when(http.execute(any(HttpUriRequest.class)))
                .thenReturn(r[0], Arrays.copyOfRange(r, 1, r.length));
    }

    void sessionClient() {
        when(clients.getHttpClient("session", false, 15))
                .thenReturn(new HttpClientSession(http, new BasicCookieStore()));
    }

    @Test
    void successfulCampusExchangeStoresSessionAndReturnsWithoutFalseError() throws Exception {
        sessionClient();
        responses(
                response(
                        200,
                        "<input id='tokens' value='synthetic'><input id='stamp' value='synthetic'>",
                        ""),
                response(
                        200,
                        "<body bgcolor='white'><a"
                            + " href='https://security.gdei.edu.cn/fixture'>continue</a></body>",
                        ""),
                response(302, "", "newpages/b.html"),
                response(302, "", "https://security.gdei.edu.cn/fixture"),
                response(200, "<a href='http://jwgl.gdei.edu.cn/fixture'>continue</a>", ""),
                response(200, "<a href='fixture.aspx'>continue</a>", ""),
                response(
                        200,
                        "<script>var student='10000000001';</script><form id='Form1'"
                            + " action='fixture?i=owner&amp;k=synthetic-key&amp;timestamp=12'></form>",
                        ""),
                response(200, "verified", ""),
                response(200, "<title>正方教务管理系统</title>", ""),
                response(200, "profile", ""));
        service.syncUpdateSessionCertificate("session", "owner", "synthetic-password");
        ArgumentCaptor<UserCertificateEntity> c =
                ArgumentCaptor.forClass(UserCertificateEntity.class);
        verify(dao).saveUserSessionCertificate(eq("session"), c.capture());
        assertEquals("owner", c.getValue().getUser().getUsername());
        assertEquals("10000000001", c.getValue().getNumber());
        assertEquals("synthetic-key", c.getValue().getKeycode());
        assertEquals(12L, c.getValue().getTimestamp());
        verify(http).close();
    }

    @Test
    void existingSessionAvoidsUpstreamAuthentication() throws Exception {
        UserCertificateEntity e = new UserCertificateEntity();
        when(dao.queryUserSessionCertificate("session")).thenReturn(e);
        assertSame(e, service.getUserSessionCertificate("session"));
        service.syncUpdateSessionCertificate("session", "owner", "synthetic");
        service.asyncUpdateSessionCertificate("session", new User("owner"));
        verifyNoInteractions(clients);
    }

    @Test
    void missingSessionRefreshesUsingCurrentLoginAndReadsNewCredential() throws Exception {
        UserCertificateEntity e = new UserCertificateEntity();
        User u = new User("owner");
        u.setPassword("synthetic");
        when(dao.queryUserSessionCertificate("session")).thenReturn(null, e, e);
        when(dao.queryUserLoginCertificate("session")).thenReturn(u);
        assertSame(e, service.getUserSessionCertificate("session"));
    }

    @Test
    void malformedLoginDoesNotSubmitPasswordAndClosesClient() throws Exception {
        sessionClient();
        responses(response(200, "<body>changed</body>", ""));
        assertThrows(
                ServerErrorException.class,
                () -> service.syncUpdateSessionCertificate("session", "owner", "synthetic"));
        verify(http, times(1)).execute(any(HttpUriRequest.class));
        verify(dao, never()).saveUserSessionCertificate(anyString(), any());
        verify(http).close();
    }

    @Test
    void untrustedRedirectCannotBeFollowed() throws Exception {
        sessionClient();
        responses(
                response(200, "<input id='tokens'><input id='stamp'>", ""),
                response(
                        200,
                        "<body bgcolor='white'><a"
                            + " href='https://untrusted.invalid/steal'>continue</a></body>",
                        ""));
        assertThrows(
                ServerErrorException.class,
                () -> service.syncUpdateSessionCertificate("session", "owner", "synthetic"));
        verify(http, times(2)).execute(any(HttpUriRequest.class));
        verify(dao, never()).saveUserSessionCertificate(anyString(), any());
    }

    @Test
    void badPasswordAndNetworkFailureAreTypedAndCloseClient() throws Exception {
        sessionClient();
        responses(
                response(200, "<input id='tokens'><input id='stamp'>", ""),
                response(200, "<body>denied</body>", ""));
        assertThrows(
                PasswordIncorrectException.class,
                () -> service.syncUpdateSessionCertificate("session", "owner", "synthetic"));
        reset(http);
        doThrow(new java.io.IOException()).when(http).execute(any(HttpUriRequest.class));
        assertThrows(
                NetWorkTimeoutException.class,
                () -> service.syncUpdateSessionCertificate("session", "owner", "synthetic"));
        verify(http).close();
    }

    @Test
    void logoutAndUserWideRevocationAreScopedAndIgnoreEmptyIdentity() {
        service.clearUserLoginAndSession(null);
        service.clearUserLoginAndSession("");
        service.clearReusableCredentials(null);
        service.clearReusableCredentials("");
        verifyNoInteractions(dao);
        service.clearUserLoginAndSession("session");
        verify(dao).deleteUserLoginCertificate("session");
        verify(dao).deleteUserSessionCertificate("session");
        service.clearReusableCredentials("owner");
        verify(dao).deleteUserSessionCertificatesByUsername("owner");
        verify(dao).deleteUserLoginCertificatesByUsername("owner");
    }

    @Test
    void credentialEntryPointsKeepCookieAndLoginNamespacesSeparate() {
        User u = new User("owner");
        when(dao.queryUserCookieCertificate("cookie")).thenReturn(u);
        when(dao.queryUserLoginCertificate("session")).thenReturn(u);
        assertSame(u, service.getUserCookieCertificate("cookie"));
        assertSame(u, service.getUserLoginCertificate("session"));
        service.saveUserCookieCertificate("cookie", "owner", "synthetic");
        service.saveUserLoginCertificate("session", "owner", "synthetic");
        service.updateUserCookieExpiration("cookie");
        service.updateUserLoginExpiration("session");
        verify(dao).saveUserCookieCertificate("cookie", "owner", "synthetic");
        verify(dao).saveUserLoginCertificate("session", "owner", "synthetic");
        verify(dao).updateUserCookieCertificateExpiration("cookie");
        verify(dao).updateUserLoginCertificateExpiration("session");
    }
}
