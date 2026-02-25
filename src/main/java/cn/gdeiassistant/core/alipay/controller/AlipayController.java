package cn.gdeiassistant.core.alipay.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.alipay.service.AlipayService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@RestController
@Deprecated
public class AlipayController {

    @Autowired
    private AlipayService alipayService;

    /**
     * 获取支付宝用户唯一标识信息
     *
     * @param authorizationCode
     * @return
     */
    @RequestMapping(value = "/alipay/app/userid", method = RequestMethod.POST)
    public DataJsonResult<String> alipayUser(@NotBlank @RequestParam("code") String authorizationCode) throws Exception {
        String id = alipayService.getAlipayUserId(authorizationCode);
        return new DataJsonResult<>(true, id);
    }
}
