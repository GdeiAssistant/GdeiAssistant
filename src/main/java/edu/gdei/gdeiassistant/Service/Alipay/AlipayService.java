package edu.gdei.gdeiassistant.Service.Alipay;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AlipayService {

    private String appid;

    private String publicKey;

    private String privateKey;

    @Value("#{propertiesReader['alipay.app.appid']}")
    public void setAppid(String appid) {
        this.appid = appid;
    }

    @Value("#{propertiesReader['alipay.app.rsa2.privatekey']}")
    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    @Value("#{propertiesReader['alipay.app.rsa2.publickey']}")
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param authorizationCode
     * @return
     */
    public String GetAlipayUserID(String authorizationCode) throws ServerErrorException, AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do", appid
                , privateKey, "json", "GBK", publicKey, "RSA2");
        AlipaySystemOauthTokenRequest request = new AlipaySystemOauthTokenRequest();
        request.setGrantType("authorization_code");
        request.setCode(authorizationCode);
        AlipaySystemOauthTokenResponse response = alipayClient.execute(request);
        if (response.isSuccess()) {
            return response.getUserId();
        }
        throw new ServerErrorException("获取支付宝用户ID异常");
    }
}
