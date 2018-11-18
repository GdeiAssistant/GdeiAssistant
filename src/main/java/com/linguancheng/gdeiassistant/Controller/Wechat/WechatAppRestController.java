package com.linguancheng.gdeiassistant.Controller.Wechat;

import com.linguancheng.gdeiassistant.Pojo.Entity.WechatUser;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatAppService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WechatAppRestController {

    @Autowired
    private WechatAppService wechatAppService;

    /**
     * 获取微信小程序用户唯一标识信息
     *
     * @param js_code
     * @return
     */
    @RequestMapping(value = "/wechat/app/userid", method = RequestMethod.POST)
    public DataJsonResult<WechatUser> WechatUser(@Validated @NotBlank @RequestParam("code") String js_code) throws Exception {
        WechatUser wechatUser = wechatAppService.GetWechatUser(js_code);
        return new DataJsonResult<>(true, wechatUser);
    }
}
