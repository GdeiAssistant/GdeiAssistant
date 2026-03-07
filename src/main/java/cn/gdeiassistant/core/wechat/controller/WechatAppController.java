package cn.gdeiassistant.core.wechat.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.wechat.pojo.vo.WechatUserVO;
import cn.gdeiassistant.core.wechat.service.WechatAppService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WechatAppController {

    @Autowired
    private WechatAppService wechatAppService;

    /**
     * 获取微信小程序用户唯一标识信息
     *
     * @param js_code
     * @return
     */
    @RequestMapping(value = "/api/wechat/app/userid", method = RequestMethod.POST)
    public DataJsonResult<WechatUserVO> wechatUser(@Validated @NotBlank @RequestParam("code") String js_code) throws Exception {
        WechatUserVO vo = wechatAppService.GetWechatUser(js_code);
        return new DataJsonResult<>(true, vo);
    }
}
