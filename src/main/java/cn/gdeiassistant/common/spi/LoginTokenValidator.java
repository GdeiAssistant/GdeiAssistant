package cn.gdeiassistant.common.spi;

import cn.gdeiassistant.common.exception.tokenvalidexception.SuspiciouseRequestException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException;
import cn.gdeiassistant.common.exception.tokenvalidexception.TokenNotMatchingException;
import cn.gdeiassistant.common.pojo.entity.Device;

/**
 * Contract used by the authentication aspect. The token domain owns the implementation.
 */
public interface LoginTokenValidator {

    void saveDevice(String signature, Device device);

    void validToken(String signature) throws TokenNotMatchingException, TokenExpiredException;

    void validDevice(String signature, String ip, Device device)
            throws TokenExpiredException, SuspiciouseRequestException;
}
