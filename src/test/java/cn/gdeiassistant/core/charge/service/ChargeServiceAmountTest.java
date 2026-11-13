package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.chargeexception.AmountNotAvailableException;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChargeServiceAmountTest {
    @Test void validAmountStillRequiresSuccessfulUpstreamAuthentication() throws Exception {
        UserCertificateService certificates = mock(UserCertificateService.class);
        var certificate = new cn.gdeiassistant.common.pojo.entity.UserCertificateEntity();
        certificate.setUser(new cn.gdeiassistant.common.pojo.entity.User("synthetic-user", "synthetic-password"));
        when(certificates.getUserSessionCertificate("synthetic-session")).thenReturn(certificate);
        var pool = mock(cn.gdeiassistant.integration.httpclient.HttpClientUtils.class);
        var client = mock(org.apache.http.impl.client.CloseableHttpClient.class);
        var response = mock(org.apache.http.client.methods.CloseableHttpResponse.class);
        when(response.getStatusLine()).thenReturn(new org.apache.http.message.BasicStatusLine(org.apache.http.HttpVersion.HTTP_1_1, 503, "Synthetic failure"));
        when(response.getEntity()).thenReturn(new org.apache.http.entity.StringEntity("Unauthenticated"));
        when(client.execute(any(org.apache.http.client.methods.HttpUriRequest.class))).thenReturn(response);
        when(pool.getHttpClient("synthetic-session", false, 15)).thenReturn(
                new cn.gdeiassistant.integration.httpclient.HttpClientSession(client, new org.apache.http.impl.client.BasicCookieStore()));
        ChargeService service = new ChargeService(pool, "https://security.gdei.edu.cn/cas/login", "http://ecard.gdei.edu.cn:8050/LoginCas.aspx", "http://ecard.gdei.edu.cn", "https://epay.gdei.edu.cn:8443/synpay/web", "https://mapi.alipay.com/gateway.do?_input_charset=utf-8", null, certificates);
        assertThrows(cn.gdeiassistant.common.exception.commonexception.ServerErrorException.class,
                () -> service.chargeRequest("synthetic-session", 50));
        verify(client, times(1)).execute(any(org.apache.http.client.methods.HttpUriRequest.class));
        verify(client, never()).execute(any(org.apache.http.client.methods.HttpPost.class));
    }

    @Test void rejectsInvalidAmountBeforeAccessingCredentialsOrTheNetwork() {
        UserCertificateService certificates = mock(UserCertificateService.class);
        ChargeService service = new ChargeService(null, null, null, null, null, null, null, certificates);
        for (int amount : new int[]{Integer.MIN_VALUE, 0, 501, Integer.MAX_VALUE}) {
            assertThrows(AmountNotAvailableException.class, () -> service.chargeRequest("synthetic-session", amount));
        }
        verifyNoInteractions(certificates);
    }
}
