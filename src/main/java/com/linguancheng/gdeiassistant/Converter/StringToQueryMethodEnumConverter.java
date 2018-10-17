package com.linguancheng.gdeiassistant.Converter;

import com.linguancheng.gdeiassistant.Enum.Query.QueryMethodEnum;
import org.springframework.core.convert.converter.Converter;

/**
 * ��String�ַ�������ת��ΪLoginMethodEnumö������
 */
public class StringToQueryMethodEnumConverter implements
        Converter<String, QueryMethodEnum> {

    @Override
    public QueryMethodEnum convert(String source) {
        return QueryMethodEnum.getEnumByValue(source);
    }
}
