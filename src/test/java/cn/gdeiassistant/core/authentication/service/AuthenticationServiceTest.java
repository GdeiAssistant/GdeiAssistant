package cn.gdeiassistant.core.authentication.service;

import cn.gdeiassistant.common.pojo.entity.Authentication;
import cn.gdeiassistant.common.exception.authenticationexception.InconsistentAuthenticationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthenticationServiceTest {

    private final AuthenticationService service = new AuthenticationService();

    @Test
    void nullTypeIsRejectedBeforeArrayLookup() {
        Authentication authentication = new Authentication();

        assertThrows(InconsistentAuthenticationException.class,
                () -> service.updateAuthentication("session-1", authentication, null));
    }

    @Test
    void outOfRangeTypeIsRejectedBeforeArrayLookup() {
        Authentication authentication = new Authentication();
        authentication.setType(99);

        assertThrows(InconsistentAuthenticationException.class,
                () -> service.updateAuthentication("session-1", authentication, null));
    }
}
