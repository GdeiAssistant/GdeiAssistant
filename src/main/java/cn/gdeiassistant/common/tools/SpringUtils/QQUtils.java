package cn.gdeiassistant.common.tools.SpringUtils;

import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Config.QQMiniProgramConfig;
import cn.gdeiassistant.common.pojo.Entity.QQUser;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

//@Component
@Deprecated
public class QQUtils {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private QQMiniProgramConfig qqMiniProgramConfig;

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param js_code
     * @return
     */
    public QQUser GetQQUser(String js_code) throws Exception {
        ResponseEntity<String> responseEntity = restTemplate.exchange("https://api.q.qq.com/sns/jscode2session?appid="
                        + qqMiniProgramConfig.getAppId() + "&secret=" + qqMiniProgramConfig.getAppSecret() + "&js_code=" + js_code + "&grant_type=authorization_code", HttpMethod.GET
                , new HttpEntity<>(new HttpHeaders()), String.class);
        JSONObject jsonObject = JSON.parseObject(responseEntity.getBody());
        if (jsonObject.containsKey("openid")) {
            QQUser qqUser = new QQUser();
            qqUser.setOpenid(jsonObject.getString("openid"));
            if (jsonObject.containsKey("unionid")) {
                qqUser.setUnionid(jsonObject.getString("unionid"));
            }
            return qqUser;
        }
        throw new ServerErrorException("获取QQ用户ID异常");
    }
}
