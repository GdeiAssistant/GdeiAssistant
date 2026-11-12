package cn.gdeiassistant.common.spi;

import cn.gdeiassistant.common.pojo.entity.User;

/**
 * Contract the common web layer needs in order to resolve a JWT session into a user.
 * Implemented by the userlogin domain; {@code common} never imports {@code core}.
 */
public interface SessionCertificateProvider {

    User getUserLoginCertificate(String sessionId);

    /**
     * Whether the account behind a still-valid Redis session can be used.
     * Keeping this here stops a servlet filter from reaching into a MyBatis mapper.
     */
    boolean isAccountActive(String username);
}
