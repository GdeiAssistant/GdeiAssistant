package edu.gdei.gdeiassistant.Controller.Alipay;

import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.Alipay.AlipayService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AlipayRestController {

    @Autowired
    private AlipayService alipayService;

    /**
     * 获取支付宝用户唯一标识信息
     *
     * @param authorizationCode
     * @return
     */
    @RequestMapping(value = "/alipay/app/userid", method = RequestMethod.POST)
    public DataJsonResult<String> AlipayUser(@NotBlank @RequestParam("code") String authorizationCode) throws Exception {
        String id = alipayService.GetAlipayUserID(authorizationCode);
        return new DataJsonResult<>(true, id);
    }
}
