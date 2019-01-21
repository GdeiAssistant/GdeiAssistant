package com.linguancheng.gdeiassistant.Converter.EnumConvert;

import com.linguancheng.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import org.springframework.core.convert.converter.Converter;

public class StringToAuthenticationTypeEnumConverter implements Converter<String, AuthenticationTypeEnum> {

    @Override
    public AuthenticationTypeEnum convert(String source) {
        for (AuthenticationTypeEnum authenticationTypeEnum : AuthenticationTypeEnum.values()) {
            if (authenticationTypeEnum.getType().equals(source)) {
                return authenticationTypeEnum;
            }
        }
        return null;
    }
}
