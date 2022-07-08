package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Pojo.Config.AlipayConfig;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipaySystemOauthTokenRequest;
import com.alipay.api.response.AlipaySystemOauthTokenResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AlipayUtils {

    @Autowired
    private AlipayConfig alipayConfig;

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param authorizationCode
     * @return
     */
    public String GetAlipayUserID(String authorizationCode) throws ServerErrorException, AlipayApiException {
        AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do"
                , alipayConfig.getAppid(), alipayConfig.getPrivateKey(), "json", "GBK"
                , alipayConfig.getPublicKey(), "RSA2");
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
