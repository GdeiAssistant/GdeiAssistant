package com.linguancheng.gdeiassistant.Converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.linguancheng.gdeiassistant.Annotation.EncryptData;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Tools.AESUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Base64;

public class EncryptDataMappingJackson2HttpMessageConverter extends MappingJackson2HttpMessageConverter {

    private Log log = LogFactory.getLog(EncryptDataMappingJackson2HttpMessageConverter.class);

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        if (o instanceof DataJsonResult) {
            DataJsonResult dataJsonResult = (DataJsonResult) o;
            if (dataJsonResult.getData() != null) {
                Annotation[] annotations = dataJsonResult.getData().getClass().getAnnotations();
                for (Annotation annotation : annotations) {
                    //若Data属性对应类有EncryptJsonData注解，则表示需要对DataJsonResult中的Data属性进行加密
                    if (annotation.getClass().equals(EncryptData.class)) {
                        if (StringUtils.isNotBlank(((EncryptData) annotation).key())) {
                            //若注解的密钥属性值不为空，则进行加密
                            try {
                                String data = Base64.getEncoder().encodeToString(AESUtils.EncryptByte(Base64.getDecoder().decode(((EncryptData) annotation).key())
                                        , new Gson().toJson(dataJsonResult.getData()).getBytes()));
                                //构建生成加密后的DataJsonResult
                                DataJsonResult<String> result = new DataJsonResult<>();
                                result.setCode(dataJsonResult.getCode());
                                result.setMessage(dataJsonResult.getMessage());
                                result.setData(data);
                                result.setSuccess(dataJsonResult.isSuccess());
                                //使用Jackson的ObjectMapper将Java对象转换成JsonString
                                ObjectMapper mapper = new ObjectMapper();
                                String json = mapper.writeValueAsString(result);
                                //输出Json字符串信息
                                outputMessage.getBody().write(json.getBytes());
                                return;
                            } catch (Exception e) {
                                log.error("加密DataJsonResult异常：", e);
                            }
                        }
                    }
                }
            }
        }
        super.writeInternal(o, outputMessage);
    }
}
