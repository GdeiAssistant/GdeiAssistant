package com.linguancheng.gdeiassistant.Converter.HttpMessageConvert;

import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.util.ArrayList;
import java.util.List;

public class WeChatMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    public WeChatMappingJackson2HttpMessageConverter() {
        List<MediaType> mediaTypes = new ArrayList<>();
        //添加TEXT/HTML类型的支持
        mediaTypes.add(MediaType.TEXT_PLAIN);
        mediaTypes.add(MediaType.TEXT_HTML);
        setSupportedMediaTypes(mediaTypes);
    }
}
