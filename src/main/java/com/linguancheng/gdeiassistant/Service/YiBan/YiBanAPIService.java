package com.linguancheng.gdeiassistant.Service.YiBan;

import com.google.gson.Gson;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Entity.YiBanUser;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
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
     * 通过Token获取用户UserID
     *
     * @param accessToken
     * @return
     */
    public BaseResult<YiBanUser, BoolResultEnum> getYiBanUserInfo(String accessToken) {
        BaseResult<YiBanUser, BoolResultEnum> result = new BaseResult<>();
        JSONObject jsonObject = restTemplate.getForObject("https://openapi.yiban.cn/user/me?access_token=" + accessToken, JSONObject.class);
        if (jsonObject.has("status") && jsonObject.getBoolean("status")) {
            YiBanUser yibanUser = new Gson().fromJson(jsonObject.toString(), YiBanUser.class);
            result.setResultData(yibanUser);
            result.setResultType(BoolResultEnum.SUCCESS);
        } else {
            result.setResultType(BoolResultEnum.ERROR);
        }
        return result;
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
