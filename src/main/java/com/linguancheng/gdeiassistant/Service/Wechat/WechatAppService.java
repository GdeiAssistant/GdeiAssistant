package com.linguancheng.gdeiassistant.Service.Wechat;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.WechatUser;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WechatAppService {

    private String appId;

    private String appSecret;

    @Value("#{propertiesReader['wechat.app.appid']}")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Value("#{propertiesReader['wechat.app.secret']}")
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param js_code
     * @return
     */
    public BaseResult<WechatUser, BoolResultEnum> GetWechatUser(String js_code) {
        BaseResult<WechatUser, BoolResultEnum> result = new BaseResult<>();
        JSONObject jsonObject = restTemplate.getForObject("https://api.weixin.qq.com/sns/jscode2session?appid="
                + appId + "&secret=" + appSecret + "&js_code="
                + js_code + "&grant_type=authorization_code", JSONObject.class);
        if (jsonObject.has("openid")) {
            WechatUser wechatUser = new WechatUser();
            wechatUser.setOpenid(jsonObject.getString("openid"));
            if (jsonObject.has("unionid")) {
                wechatUser.setUnionid(jsonObject.getString("unionid"));
            }
            result.setResultData(wechatUser);
            result.setResultType(BoolResultEnum.SUCCESS);
        } else {
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
    }
}
