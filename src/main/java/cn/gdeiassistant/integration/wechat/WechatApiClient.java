package cn.gdeiassistant.integration.wechat;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信官方 API 防腐层：封装所有对 api.weixin.qq.com 的 HTTP 调用，只向上返回 JSON 字符串或二进制数据。
 */
@Component
public class WechatApiClient {

    @Autowired
    private RestTemplate restTemplate;

    /**
     * OAuth2.0 授权：使用 code 换取 access_token 与 openid，返回原始 JSON 字符串。
     */
    public String getOauth2AccessTokenJson(String appId, String appSecret, String code) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                        + appId + "&secret=" + appSecret + "&code=" + code
                        + "&grant_type=authorization_code",
                String.class
        );
    }

    /**
     * 获取公众号全局 access_token，返回原始 JSON 字符串。
     */
    public String getAccessTokenJson(String appId, String appSecret) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential"
                        + "&appid=" + appId
                        + "&secret=" + appSecret,
                String.class
        );
    }

    /**
     * 获取 JS-SDK 所需 jsapi_ticket 的 JSON。
     */
    public String getJsApiTicketJson(String accessToken) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="
                        + accessToken + "&type=jsapi",
                String.class
        );
    }

    /**
     * 下载 JSSDK 上传的音频，只有在 Content-Type 为 voice/speex 时返回二进制数据，其余返回 null。
     */
    public byte[] downloadJssdkVoice(String accessToken, String voiceId) {
        ResponseEntity<byte[]> responseEntity = restTemplate.exchange(
                "https://api.weixin.qq.com/cgi-bin/media/get/jssdk?access_token="
                        + accessToken + "&media_id=" + voiceId,
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                byte[].class
        );
        String contentType = responseEntity.getHeaders().getFirst("Content-Type");
        if ("voice/speex".equals(contentType)) {
            return responseEntity.getBody();
        }
        return null;
    }

    /**
     * 获取微信用户基本信息（unionid 等），返回原始 JSON 字符串。
     */
    public String getUserInfoJson(String accessToken, String openid) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/sns/userinfo?access_token="
                        + accessToken + "&openid=" + openid,
                String.class
        );
    }

    /**
     * 获取素材总数（含 news_count），返回 JSON 字符串。
     */
    public String getMaterialCountJson(String accessToken) {
        return restTemplate.getForObject(
                "https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token="
                        + accessToken,
                String.class
        );
    }

    /**
     * 批量获取图文素材列表，返回 JSON 字符串。
     */
    public String batchGetMaterialJson(String accessToken, JSONObject params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForObject(
                "https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token="
                        + accessToken,
                new HttpEntity<>(params, headers),
                String.class
        );
    }

    /**
     * 小程序 jscode2session，返回原始 JSON 字符串。
     */
    public String jscode2SessionJson(String appId, String appSecret, String jsCode) {
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                "https://api.weixin.qq.com/sns/jscode2session?appid="
                        + appId + "&secret=" + appSecret + "&js_code="
                        + jsCode + "&grant_type=authorization_code",
                HttpMethod.GET,
                new HttpEntity<>(new HttpHeaders()),
                String.class
        );
        return responseEntity.getBody();
    }
}

