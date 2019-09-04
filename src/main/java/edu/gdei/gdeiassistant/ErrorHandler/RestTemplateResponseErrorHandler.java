package edu.gdei.gdeiassistant.ErrorHandler;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;

public class RestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        //不抛出异常，由客户端根据HTTP状态码进行处理
    }
}
