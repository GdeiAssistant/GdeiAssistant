package cn.gdeiassistant.Converter.EnumConvert;

import cn.gdeiassistant.Enum.Authentication.AuthenticationTypeEnum;
import org.springframework.core.convert.converter.Converter;

public class StringToAuthenticationTypeEnumConverter implements Converter<String, AuthenticationTypeEnum> {

    @Override
    public AuthenticationTypeEnum convert(String source) {
        return AuthenticationTypeEnum.getEnumByValue(source);
    }
}
