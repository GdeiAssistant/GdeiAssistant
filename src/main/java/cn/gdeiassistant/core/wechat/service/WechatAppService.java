package cn.gdeiassistant.core.wechat.service;

import cn.gdeiassistant.common.exception.CommonException.FeatureNotEnabledException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.wechat.pojo.vo.WechatUserVO;
import cn.gdeiassistant.integration.wechat.WechatApiClient;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class WechatAppService {

    private String appId;

    private String appSecret;

    @Value("${wechat.app.appid:}")
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Value("${wechat.app.secret:}")
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    @Autowired
    private WechatApiClient wechatApiClient;

    /**
     * 使用登录凭证获取用户标识信息
     */
    public WechatUserVO GetWechatUser(String js_code) throws Exception {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(appSecret)) {
            throw new FeatureNotEnabledException("微信功能未启用");
        }
        String body = wechatApiClient.jscode2SessionJson(appId, appSecret, js_code);
        JSONObject jsonObject = JSON.parseObject(body);
        if (jsonObject.containsKey("openid")) {
            WechatUserVO vo = new WechatUserVO();
            vo.setOpenid(jsonObject.getString("openid"));
            if (jsonObject.containsKey("unionid")) {
                vo.setUnionid(jsonObject.getString("unionid"));
            }
            return vo;
        }
        throw new ServerErrorException("获取微信ID异常");
    }
}
