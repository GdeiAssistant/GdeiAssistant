package cn.gdeiassistant.core.charge.service;

import cn.gdeiassistant.common.exception.chargeexception.AmountNotAvailableException;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ChargeServiceAmountTest {
    @Test void rejectsInvalidAmountBeforeAccessingCredentialsOrTheNetwork() {
        ChargeService service = new ChargeService();
        UserCertificateService certificates = mock(UserCertificateService.class);
        ReflectionTestUtils.setField(service, "userCertificateService", certificates);
        for (int amount : new int[]{Integer.MIN_VALUE, 0, 501, Integer.MAX_VALUE}) {
            assertThrows(AmountNotAvailableException.class, () -> service.chargeRequest("synthetic-session", amount));
        }
        verifyNoInteractions(certificates);
    }
}
