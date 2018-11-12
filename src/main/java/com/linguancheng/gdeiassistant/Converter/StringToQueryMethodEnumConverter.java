package com.linguancheng.gdeiassistant.Converter;

import com.linguancheng.gdeiassistant.Enum.Base.QueryMethodEnum;
import org.springframework.core.convert.converter.Converter;

/**
 * 将String字符串类型转换为QueryMethodEnum枚举类型
 */
public class StringToQueryMethodEnumConverter implements
        Converter<String, QueryMethodEnum> {

    @Override
    public QueryMethodEnum convert(String source) {
        return QueryMethodEnum.getEnumByValue(source);
    }
}
