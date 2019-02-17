package edu.gdei.gdeiassistant.Converter.EnumConvert;

import edu.gdei.gdeiassistant.Enum.Base.LoginMethodEnum;
import org.springframework.core.convert.converter.Converter;

/**
 * 将String字符串类型转换为LoginMethodEnum枚举类型
 */
public class StringToLoginMethodEnumConverter implements Converter<String, LoginMethodEnum> {

    @Override
    public LoginMethodEnum convert(String source) {
        return LoginMethodEnum.getEnumByValue(source);
    }
}
