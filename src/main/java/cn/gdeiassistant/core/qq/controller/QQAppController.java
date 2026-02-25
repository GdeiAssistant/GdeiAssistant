package cn.gdeiassistant.core.qq.controller;

import cn.gdeiassistant.common.pojo.Entity.QQUser;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.qq.service.QQAppService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

//@RestController
@Deprecated
public class QQAppController {

    @Autowired
    private QQAppService qqAppService;

    /**
     * 获取微信小程序用户唯一标识信息
     *
     * @param js_code
     * @return
     */
    @RequestMapping(value = "/qq/app/userid", method = RequestMethod.POST)
    public DataJsonResult<QQUser> qqUser(@Validated @NotBlank @RequestParam("code") String js_code) throws Exception {
        QQUser qqUser = qqAppService.getQQUser(js_code);
        return new DataJsonResult<>(true, qqUser);
    }
}
