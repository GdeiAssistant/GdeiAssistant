package edu.gdei.gdeiassistant.Service.YiBan;

import com.google.gson.Gson;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Entity.YiBanUser;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class YiBanAPIService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserLoginService userLoginService;

    /**
     * 通过凭证令牌获取易班用户信息
     *
     * @param accessToken
     * @return
     */
    public YiBanUser getYiBanUserInfo(String accessToken) throws ServerErrorException {
        JSONObject jsonObject = restTemplate.getForObject("https://openapi.yiban.cn/user/me?access_token=" + accessToken, JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
            return new Gson().fromJson(jsonObject.toString(), YiBanUser.class);
        }
        throw new ServerErrorException("易班网系统异常");
    }

    /**
     * 获取易班网校方认证信息
     *
     * @param accessToken
     * @return
     */
    public Map<String, String> getYiBanVerifyInfo(String accessToken) throws ServerErrorException {
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = restTemplate.getForObject("https://openapi.yiban.cn/user/verify_me?access_token=" + accessToken
                , JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getString("status").equals("success")) {
            String name = jsonObject.getJSONObject("info").getString("yb_realname");
            String number = jsonObject.getJSONObject("info").getString("yb_studentid");
            map.put("name", name);
            map.put("number", number);
            return map;
        }
        throw new ServerErrorException("易班网系统异常");
    }

    /**
     * 易班已绑定教务系统账号的用户通过用户名快速登入教务系统
     *
     * @param sessionId
     * @param username
     * @return
     */
    public User YiBanQuickLogin(String sessionId, String username) throws Exception {
        User user = userMapper.selectUser(StringEncryptUtils.encryptString(username));
        if (user != null) {
            user = user.decryptUser();
            UserCertificate userCertificate = userLoginService.UserLogin(sessionId, user, true);
            return userCertificate.getUser();
        }
        return null;
    }
}
