package edu.gdei.gdeiassistant.Controller.RSAEncryption.RestController;

import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Tools.RSAUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
public class RSAEncryptionRestController {

    /**
     * 获取RSA公钥密钥Base64编码字符串
     *
     * @return
     */
    @RequestMapping(value = "/rest/encryption/rsa/publickey", method = RequestMethod.GET)
    public DataJsonResult<String> GetRSAPublicKey() {
        return new DataJsonResult<>(true, Base64.getEncoder().encodeToString(RSAUtils.getPublicKey()));
    }

}
