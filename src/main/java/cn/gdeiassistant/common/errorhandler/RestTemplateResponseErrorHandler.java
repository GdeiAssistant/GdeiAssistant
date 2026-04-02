package cn.gdeiassistant.common.errorhandler;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;

import java.io.IOException;
import java.net.URI;

public class RestTemplateResponseErrorHandler extends DefaultResponseErrorHandler {

    @Override
    public void handleError(URI url, org.springframework.http.HttpMethod method, ClientHttpResponse response) throws IOException {
        //不抛出异常，由客户端根据HTTP状态码进行处理
    }
}
