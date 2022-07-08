package cn.gdeiassistant.Service.ThirdParty.Alipay;

import cn.gdeiassistant.Exception.CommonException.ServerErrorException;
import cn.gdeiassistant.Tools.SpringUtils.AlipayUtils;
import com.alipay.api.AlipayApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlipayService {

    @Autowired
    private AlipayUtils alipayUtils;

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param authorizationCode
     * @return
     * @throws ServerErrorException
     * @throws AlipayApiException
     */
    public String GetAlipayUserID(String authorizationCode) throws ServerErrorException, AlipayApiException {
        return alipayUtils.GetAlipayUserID(authorizationCode);
    }

}
