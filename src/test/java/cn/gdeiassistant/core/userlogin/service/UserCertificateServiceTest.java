package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.common.redis.UserCertificate.UserCertificateDao;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UserCertificateServiceTest {

    @Test
    void sessionRefreshLooksUpCertificateBySessionId() throws Exception {
        UserCertificateDao dao = mock(UserCertificateDao.class);
        when(dao.queryUserSessionCertificate("session-1"))
                .thenReturn(new UserCertificateEntity());

        UserCertificateService service = new UserCertificateService();
        ReflectionTestUtils.setField(service, "userCertificateDao", dao);

        service.syncUpdateSessionCertificate("session-1", "username", "password");

        verify(dao).queryUserSessionCertificate("session-1");
        verifyNoMoreInteractions(dao);
    }
}
