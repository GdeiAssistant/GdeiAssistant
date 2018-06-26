package com.linguancheng.gdeiassistant.Service.YiBan;

import com.google.gson.Gson;
import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Entity.YiBanUser;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Service.UserLogin.UserLoginService;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Service
public class YiBanLoginService {

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private UserLoginService userLoginService;

    @Autowired
    private HttpClientFactory httpClientFactory;

    private Log log = LogFactory.getLog(YiBanLoginService.class);

    /**
     * 清除登录用户的登录记录凭证
     *
     * @param request
     */
    public void ClearUserLoginCredentials(HttpServletRequest request) {
        httpClientFactory.ClearCookies(request.getSession());
    }

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
     * 易班已绑定教务系统账号的用户通过用户名快速登入教务系统
     *
     * @param username
     * @return
     */
    public BaseResult<User, LoginResultEnum> YiBanQuickLogin(HttpServletRequest request, String username) {
        BaseResult<User, LoginResultEnum> result = new BaseResult<>();
        try {
            User user = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (user != null) {
                String decryptedUsername = StringEncryptUtils.decryptString(user.getUsername());
                String decryptedPassword = StringEncryptUtils.decryptString(user.getPassword());
                BaseResult<User, LoginResultEnum> userLoginResult = userLoginService
                        .UserLogin(request, new User(decryptedUsername, decryptedPassword), true);
                switch (userLoginResult.getResultType()) {
                    case LOGIN_SUCCESS:
                        result.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                        result.setResultData(userLoginResult.getResultData());
                        break;

                    case PASSWORD_ERROR:
                        result.setResultType(LoginResultEnum.PASSWORD_ERROR);
                        break;

                    case TIME_OUT:
                        result.setResultType(LoginResultEnum.TIME_OUT);
                        break;

                    default:
                        throw new ServerErrorException();
                }
                return result;
            }
            throw new ServerErrorException();
        } catch (Exception e) {
            log.error("易班登录异常：" + e);
            result.setResultType(LoginResultEnum.SERVER_ERROR);
        }
        return result;
    }
}
