package edu.gdei.gdeiassistant.Converter.EnumConvert;

import edu.gdei.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import org.springframework.core.convert.converter.Converter;

public class IntegerToAuthenticationTypeEnumConverter implements Converter<Integer, AuthenticationTypeEnum> {

    @Override
    public AuthenticationTypeEnum convert(Integer source) {
        for (AuthenticationTypeEnum authenticationTypeEnum : AuthenticationTypeEnum.values()) {
            if (authenticationTypeEnum.getType().equals(Integer.valueOf(source))) {
                return authenticationTypeEnum;
            }
        }
        return null;
    }
}
