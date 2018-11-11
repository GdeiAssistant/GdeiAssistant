package com.linguancheng.gdeiassistant.Service.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.UserLogin.UserCertificate;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Repository.Redis.User.UserDao;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Service
public class UserLoginService {

    private Log log = LogFactory.getLog(UserLoginService.class);

    private int timeout;

    private String url;

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Autowired
    private UserDao userDao;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }


    @Value("#{propertiesReader['timeout.userlogin']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 清除登录用户的登录记录凭证
     *
     * @param request
     */
    public void ClearUserLoginCredentials(HttpServletRequest request) {
        httpClientFactory.ClearCookies(request.getSession());
    }

    /**
     * 查询数据库中用户名对应的用户账号信息
     *
     * @param username
     * @return
     */
    public BaseResult<User, DataBaseResultEnum> GetUserByUsername(String username) {
        BaseResult<User, DataBaseResultEnum> result = new BaseResult<>();
        try {
            User user = userMapper.selectUser(StringEncryptUtils.encryptString(username));
            if (user == null) {
                result.setResultType(DataBaseResultEnum.EMPTY_RESULT);
            } else {
                result.setResultData(user.decryptUser());
                result.setResultType(DataBaseResultEnum.SUCCESS);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.setResultType(DataBaseResultEnum.ERROR);
        }
        return result;
    }

    /**
     * 用户登录学院系统业务的接口
     *
     * @param request
     * @param user
     * @param quickLogin
     * @return
     */
    public BaseResult<UserCertificate, LoginResultEnum> UserLogin(HttpServletRequest request, User user, boolean quickLogin) {
        BaseResult<UserCertificate, LoginResultEnum> userLoginResult = new BaseResult<>();
        //查询数据库,若账号密码相同,则直接通过登录校验
        if (quickLogin) {
            try {
                User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(user.getUsername()));
                if (queryUser != null && !queryUser.getState().equals(-1)) {
                    //将数据库查询的用户数据与用户提交的用户信息进行对比
                    User decryptUser = queryUser.decryptUser();
                    if (StringUtils.isNotBlank(user.getPassword())) {
                        if (decryptUser.getUsername().equals(user.getUsername())
                                && decryptUser.getPassword().equals(user.getPassword())) {
                            //登录成功,缓存并返回用户信息
                            UserCertificate userCertificate = new UserCertificate();
                            userCertificate.setUser(queryUser.decryptUser());
                            userLoginResult.setResultData(userCertificate);
                            userLoginResult.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                            return userLoginResult;
                        }
                    }
                }
            } catch (Exception e) {
                //若查询数据库或加解密出现异常,则放弃数据库校验,使用学校教务系统登录校验
                log.error("用户登录数据库校验异常：", e);
            }
        }
        //用户不存在或与数据库的数据信息不匹配,进行普通登录
        CloseableHttpClient httpClient = null;
        if (StringUtils.isBlank(user.getPassword())) {
            try {
                //若用户未填入密码，则通过加密值和学号从数据库查询密码值
                User queryUser = userMapper.selectUser(StringEncryptUtils
                        .encryptString(user.getUsername()));
                if (queryUser != null && !queryUser.getState().equals(-1)) {
                    queryUser = queryUser.decryptUser();
                    if (queryUser.getUsername().equals(user.getUsername())
                            && queryUser.getKeycode().equals(user.getKeycode())
                            && queryUser.getNumber().equals(user.getNumber())) {
                        //数据库校验通过，填入数据库中保存的密码值
                        user.setPassword(queryUser.getPassword());
                    } else {
                        //数据库校验不通过，提示用户重新登录
                        userLoginResult.setResultType(LoginResultEnum.PASSWORD_ERROR);
                        return userLoginResult;
                    }
                }
            } catch (Exception e) {
                //获取密码值失败
                log.error("用户登录数据库校验异常：", e);
                userLoginResult.setResultType(LoginResultEnum.SERVER_ERROR);
                return userLoginResult;
            }
        }
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), false, timeout);
            HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login?service=http://my.gdei.edu.cn:8002/index/index.jsp");
                //封装身份认证需要POST发送的相关数据
                BasicNameValuePair basicNameValuePair_1 = new BasicNameValuePair("username", user.getUsername());
                BasicNameValuePair basicNameValuePair_2 = new BasicNameValuePair("password", user.getPassword());
                BasicNameValuePair basicNameValuePair_3 = new BasicNameValuePair("service", "http://my.gdei.edu.cn:8002/index/index.jsp");
                BasicNameValuePair basicNameValuePair_4 = new BasicNameValuePair("imageField.x", "0");
                BasicNameValuePair basicNameValuePair_5 = new BasicNameValuePair("imageField.y", "0");
                List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                //将BasicNameValuePair对象添加到ArrayList中
                basicNameValuePairs.add(basicNameValuePair_1);
                basicNameValuePairs.add(basicNameValuePair_2);
                basicNameValuePairs.add(basicNameValuePair_3);
                basicNameValuePairs.add(basicNameValuePair_4);
                basicNameValuePairs.add(basicNameValuePair_5);
                //绑定表单参数
                httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                httpResponse = httpClient.execute(httpPost);
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() != 200) {
                    //服务器异常
                    throw new ServerErrorException("教务系统异常");
                }
                if (!document.select("body").get(0).hasAttr("bgcolor")) {
                    //认证失败,提示账号或密码错误
                    throw new PasswordIncorrectException("登录账号密码不正确");
                }
                httpGet = new HttpGet(document.select("a").first().attr("href"));
                httpResponse = httpClient.execute(httpGet);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //连接到教务系统
                    return LoginCasSystem(httpClient, user);
                }
                throw new ServerErrorException("教务系统异常");
            } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                    //已经通过了认证
                    return LoginCasSystem(httpClient, user);
                }
            }
            throw new ServerErrorException("教务系统异常");
        } catch (ServerErrorException e) {
            log.error("用户登录异常：", e);
            userLoginResult.setResultType(LoginResultEnum.SERVER_ERROR);
        } catch (PasswordIncorrectException e) {
            log.error("用户登录异常：", e);
            userLoginResult.setResultType(LoginResultEnum.PASSWORD_ERROR);
        } catch (IOException e) {
            log.error("用户登录异常：", e);
            userLoginResult.setResultType(LoginResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("用户登录异常：", e);
            userLoginResult.setResultType(LoginResultEnum.SERVER_ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return userLoginResult;
    }

    /**
     * 登录教务系统,UserLoginService内部调用的模块方法
     *
     * @param httpClient
     * @param user
     * @return
     * @throws IOException
     * @throws ServerErrorException
     */
    private BaseResult<UserCertificate, LoginResultEnum> LoginCasSystem(CloseableHttpClient httpClient, User user) throws Exception {
        HttpGet httpGet = new HttpGet(url + "login_cas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 302
                && ("https://security.gdei.edu.cn/cas/login" +
                "?service=http://jwgl.gdei.edu.cn/login_cas.aspx")
                .equals(httpResponse.getFirstHeader("Location").getValue())) {
            httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login?service=http://jwgl.gdei.edu.cn/login_cas.aspx");
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            httpGet = new HttpGet(document.select("a").first().attr("href"));
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            httpGet = new HttpGet(url + document.select("a").attr("href"));
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            //获取学生的教务系统信息
            String script = document.select("script").first().data();
            String username = user.getUsername();
            String password = user.getPassword();
            String keycode = (document.getElementById("Form1").attr("action")
                    .split("&")[1]).split("=")[1];
            String number = ((script.split("=")[1]).split("'")[0]).substring(0, 11);
            Long timestamp = Long.valueOf(document.getElementById("Form1")
                    .attr("action").split("&")[2].split("=")[1]);
            //进行教务系统身份校验
            return CasVerify(httpClient, username, password, keycode, number, timestamp);
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 进行教务系统身份校验,在普通登录教务系统后最终调用或通过KeyCode快速连接直接调用
     * UserLoginService内部调用的模块方法
     *
     * @param httpClient
     * @param username
     * @param password
     * @param keycode
     * @param number
     * @param timestamp
     * @return
     * @throws IOException
     * @throws ServerErrorException
     */
    private BaseResult<UserCertificate, LoginResultEnum> CasVerify(CloseableHttpClient httpClient, String username
            , String password, String keycode, String number, Long timestamp) throws IOException, ServerErrorException {
        BaseResult<UserCertificate, LoginResultEnum> userLoginResult = new BaseResult<>();
        HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + username + "&k="
                + keycode + "&timestamp=" + timestamp);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            httpResponse.close();
            httpGet = new HttpGet(url + "xs_main.aspx?xh=" + number + "&type=1");
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200
                    && "正方教务管理系统".equals(new String(document.title()
                    .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))) {
                //获取学生的身份证号
                httpGet = new HttpGet(url + "xsgrxx.aspx?xh=" + number);
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //缓存学生的信息
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setKeycode(keycode);
                    user.setNumber(number);
                    UserCertificate userCertificate = new UserCertificate();
                    userCertificate.setUser(user);
                    userCertificate.setTimestamp(timestamp);
                    userLoginResult.setResultData(userCertificate);
                    userLoginResult.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                    return userLoginResult;
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 同步地与教务系统会话同步
     *
     * @param request
     * @return
     */
    public ServiceResultEnum SyncUpdateSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) httpSession.getAttribute("password");
        UserCertificate userCertificate = userDao.queryUserCertificate(username);
        if (userCertificate == null) {
            User user = new User(username, password);
            BaseResult<UserCertificate, LoginResultEnum> userLoginResult = UserLogin(request
                    , user, false);
            switch (userLoginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    userDao.saveUserCertificate(userLoginResult.getResultData());
                    return ServiceResultEnum.SUCCESS;

                case PASSWORD_ERROR:
                    return ServiceResultEnum.PASSWORD_INCORRECT;

                case TIME_OUT:
                    return ServiceResultEnum.TIME_OUT;

                case SERVER_ERROR:
                    return ServiceResultEnum.SERVER_ERROR;
            }
        }
        return ServiceResultEnum.SUCCESS;
    }

    /**
     * 异步地与教务系统会话进行同步
     *
     * @param request
     */
    @Async
    public void AsyncUpdateSession(HttpServletRequest request) {
        HttpSession httpSession = request.getSession();
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) httpSession.getAttribute("password");
        UserCertificate userCertificate = userDao.queryUserCertificate(username);
        if (userCertificate == null) {
            User user = new User(username, password);
            BaseResult<UserCertificate, LoginResultEnum> userLoginResult = UserLogin(request,
                    user, false);
            switch (userLoginResult.getResultType()) {
                case LOGIN_SUCCESS:
                    userDao.saveUserCertificate(userLoginResult.getResultData());
                    break;

                default:
                    //同步会话失败
                    break;
            }
        }
    }

    /**
     * 定时更新用户账号信息
     */
    @Deprecated
    public void UpdateUserData() {
        try {
            List<User> userList = userMapper.selectAllUser();
            //设置线程信号量，限制最大同时更新的线程数为10
            Semaphore semaphore = new Semaphore(10);
            for (User user : userList) {
                user = user.decryptUser();
                HttpHeaders httpHeaders = new HttpHeaders();
                httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("username", user.getUsername());
                params.add("password", user.getPassword());
                params.add("quickLogin", String.valueOf(false));
                semaphore.acquire();
                ListenableFuture<ResponseEntity<DataJsonResult<User>>> future = asyncRestTemplate
                        .exchange("https://www.gdeiassistant.cn/rest/userlogin"
                                , HttpMethod.POST, new HttpEntity<>(params, httpHeaders)
                                , new ParameterizedTypeReference<DataJsonResult<User>>() {
                                });
                future.addCallback(new ListenableFutureCallback<ResponseEntity<DataJsonResult<User>>>() {

                    @Override
                    public void onFailure(Throwable ex) {
                        log.error("定时更新用户账号信息异常：", ex);
                        semaphore.release();
                    }

                    @Override
                    public void onSuccess(ResponseEntity<DataJsonResult<User>> result) {
                        semaphore.release();
                    }
                });
            }
        } catch (Exception e) {
            log.error("定时更新用户账号信息异常：", e);
        }
    }
}
