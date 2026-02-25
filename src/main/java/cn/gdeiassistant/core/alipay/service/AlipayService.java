package cn.gdeiassistant.core.alipay.service;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.tools.SpringUtils.AlipayUtils;
import com.alipay.api.AlipayApiException;
import org.springframework.beans.factory.annotation.Autowired;

//@Service
@Deprecated
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
    public String getAlipayUserId(String authorizationCode) throws ServerErrorException, AlipayApiException {
        return alipayUtils.GetAlipayUserID(authorizationCode);
    }

}
