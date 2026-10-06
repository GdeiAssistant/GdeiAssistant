package cn.gdeiassistant.core.userlogin.service;

import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class FreshPasswordVerificationTest {
    private CloseableHttpResponse response(String html) {
        var response = mock(CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1,200,"OK"));
        when(response.getEntity()).thenReturn(new StringEntity(html,java.nio.charset.StandardCharsets.UTF_8));
        return response;
    }
    @Test
    void verificationAlwaysPostsPasswordWithIsolatedCookiesAndDoesNotUseSavedCredential() throws Exception {
        var clients = mock(HttpClientUtils.class);
        var client = mock(CloseableHttpClient.class);
        var cached = mock(UserCertificateDao.class);
        when(clients.getHttpClient(null,false,15)).thenReturn(new HttpClientSession(client,new BasicCookieStore()));
        var pageResponse = response("<input id='tokens' value='synthetic'><input id='stamp' value='synthetic'>");
        var resultResponse = response("<body bgcolor='white'><a href='http://portal.gdei.edu.cn:8001/Login'>ok</a></body>");
        when(client.execute(any(HttpUriRequest.class))).thenReturn(pageResponse, resultResponse);
        var service = new UserCertificateService();
        ReflectionTestUtils.setField(service,"httpClientUtils",clients);
        ReflectionTestUtils.setField(service,"userCertificateDao",cached);
        service.verifyCurrentPassword("synthetic-user","synthetic-password");
        verify(clients).getHttpClient(null,false,15);
        verify(client,times(2)).execute(any(HttpUriRequest.class));
        verifyNoInteractions(cached);
    }
    @Test
    void rejectedPasswordDoesNotAuthorizeDeletion() throws Exception {
        var clients = mock(HttpClientUtils.class);
        var client = mock(CloseableHttpClient.class);
        when(clients.getHttpClient(null,false,15)).thenReturn(new HttpClientSession(client,new BasicCookieStore()));
        var pageResponse = response("<input id='tokens'><input id='stamp'>");
        var resultResponse = response("<body>wrong password</body>");
        when(client.execute(any(HttpUriRequest.class))).thenReturn(pageResponse, resultResponse);
        var service = new UserCertificateService();
        ReflectionTestUtils.setField(service,"httpClientUtils",clients);
        assertThrows(PasswordIncorrectException.class, () -> service.verifyCurrentPassword("synthetic-user","wrong"));
    }
}
