package cn.gdeiassistant.core.rSAEncryption.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.tools.Utils.RSAUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class RSAEncryptionController {

    /**
     * 获取RSA公钥密钥Base64编码字符串
     *
     * @return
     */
    @RequestMapping(value = "/api/encryption/rsa/publickey", method = RequestMethod.GET)
    public DataJsonResult<String> getRSAPublicKey() {
        return new DataJsonResult<>(true, Base64.getEncoder().encodeToString(RSAUtils.getPublicKey()));
    }

}
