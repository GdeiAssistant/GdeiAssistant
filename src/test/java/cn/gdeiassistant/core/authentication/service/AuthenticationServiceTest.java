package cn.gdeiassistant.core.authentication.service;

import cn.gdeiassistant.common.pojo.Entity.Authentication;
import cn.gdeiassistant.common.exception.AuthenticationException.InconsistentAuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationServiceTest {

    private final AuthenticationService service = new AuthenticationService();

    @Test
    void nullTypeIsRejectedBeforeArrayLookup() {
        Authentication authentication = new Authentication();

        assertThrows(InconsistentAuthenticationException.class,
                () -> service.UpdateAuthentication("session-1", authentication, null));
    }

    @Test
    void outOfRangeTypeIsRejectedBeforeArrayLookup() {
        Authentication authentication = new Authentication();
        authentication.setType(99);

        assertThrows(InconsistentAuthenticationException.class,
                () -> service.UpdateAuthentication("session-1", authentication, null));
    }
}
