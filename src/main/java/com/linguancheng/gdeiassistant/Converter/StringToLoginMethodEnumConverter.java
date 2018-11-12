package com.linguancheng.gdeiassistant.Converter;

import org.springframework.core.convert.converter.Converter;
import com.linguancheng.gdeiassistant.Enum.Base.LoginMethodEnum;

/**
 * ��String�ַ�������ת��ΪLoginMethodEnumö������
 */
public class StringToLoginMethodEnumConverter implements Converter<String, LoginMethodEnum> {

    @Override
    public LoginMethodEnum convert(String source) {
        return LoginMethodEnum.getEnumByValue(source);
    }
}
