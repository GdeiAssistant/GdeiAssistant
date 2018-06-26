package com.linguancheng.gdeiassistant.Controller.Wechat;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.WechatUser;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.Wechat.WechatAppService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class WechatAppController {

    @Autowired
    private WechatAppService wechatAppService;

    /**
     * 获取微信小程序用户唯一标识信息
     *
     * @param js_code
     * @return
     */
    @RequestMapping("/wechat/app/userid")
    @ResponseBody
    public DataJsonResult<WechatUser> WechatUser(String js_code) {
        DataJsonResult<WechatUser> jsonResult = new DataJsonResult<>();
        if (StringUtils.isBlank(js_code)) {
            jsonResult.setSuccess(false);
        } else {
            BaseResult<WechatUser, BoolResultEnum> result = wechatAppService.GetWechatUser(js_code);
            switch (result.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getResultData());
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    break;
            }
        }
        return jsonResult;
    }
}
