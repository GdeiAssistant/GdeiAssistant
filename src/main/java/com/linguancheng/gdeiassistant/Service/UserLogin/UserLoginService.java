package com.linguancheng.gdeiassistant.Service.UserLogin;

import com.linguancheng.gdeiassistant.Enum.Base.LoginResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Factory.HttpClientFactory;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.ScheduleQuery.ScheduleQueryJsonResult;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User.UserMapper;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Tools.StringEncryptUtils;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.web.client.AsyncRestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Created by linguancheng on 2017/7/16.
 */

@Service
public class UserLoginService {

    private String url;

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private HttpClientFactory httpClientFactory;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }

    private Log log = LogFactory.getLog(UserLoginService.class);

    private int timeout;

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
     * 用户登录学院系统业务的接口
     *
     * @param request
     * @param user
     * @param quickLogin
     * @return
     */
    public BaseResult<User, LoginResultEnum> UserLogin(HttpServletRequest request, User user, boolean quickLogin) {
        BaseResult<User, LoginResultEnum> result = new BaseResult<>();
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
                            result.setResultData(queryUser.decryptUser());
                            result.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                            return result;
                        }
                    } else {
                        if (decryptUser.getUsername().equals(user.getUsername())
                                && decryptUser.getKeycode().equals(user.getKeycode())
                                && decryptUser.getNumber().equals(user.getNumber())) {
                            //登录成功,缓存并返回用户信息
                            result.setResultData(queryUser.decryptUser());
                            result.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                            return result;
                        }
                    }
                }
            } catch (Exception e) {
                //若查询数据库或加解密出现异常,则放弃数据库校验,使用学校教务系统登录校验
                e.printStackTrace();
            }
        }
        CloseableHttpClient httpClient = null;
        try {
            httpClient = httpClientFactory.getHttpClient(request.getSession(), timeout);
            //用户不存在或与数据库的数据信息不匹配,进行普通登录
            HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login?service=http://my.gdei.edu.cn:8002/index/index.jsp");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.title().equals("广东第二师范学院中央认证服务－登录")) {
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
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (document.title().equals("广东第二师范学院中央认证服务－登录")) {
                        //认证失败,提示账号或密码错误
                        throw new PasswordIncorrectException("登录账号密码不正确");
                    }
                    httpGet = new HttpGet(document.select("a").first().attr("href"));
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("首页")) {
                        //连接到教务系统
                        return LoginCasSystem(httpClient, user);
                    }
                    throw new ServerErrorException("教务系统异常");
                } else {
                    //自动登录
                    return LoginCasSystem(httpClient, user);
                }
            }
            throw new ServerErrorException("教务系统异常");
        } catch (ServerErrorException e) {
            log.error("用户登录异常：", e);
            result.setResultType(LoginResultEnum.SERVER_ERROR);
        } catch (PasswordIncorrectException e) {
            log.error("用户登录异常：", e);
            result.setResultType(LoginResultEnum.PASSWORD_ERROR);
        } catch (IOException e) {
            log.error("用户登录异常：", e);
            result.setResultType(LoginResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("用户登录异常：", e);
            result.setResultType(LoginResultEnum.SERVER_ERROR);
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
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
    private BaseResult<User, LoginResultEnum> LoginCasSystem(CloseableHttpClient httpClient, User user) throws Exception {
        HttpGet httpGet = new HttpGet(url + "login_cas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("»ªÄÏÀí¹¤´óÑ§ÖÐÑëÈÏÖ¤·þÎñ")) {
            httpGet = new HttpGet(document.select("a").first().attr("href"));
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().isEmpty()) {
                //获取学生的教务系统信息
                String script = document.select("script").first().data();
                String s1[] = script.split("=");
                String s2[] = s1[1].split("'");
                String system_identity = user.getUsername();
                String system_password = user.getPassword();
                String system_number = s2[0].substring(0, 11);
                String system_key = document.getElementById("Form1").toString().substring(system_identity.length() + 66, system_identity.length() + 98);
                //进行教务系统身份校验
                return CasVerify(httpClient, system_identity, system_password, system_key, system_number);
            }
            throw new ServerErrorException("教务系统异常");
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 进行教务系统身份校验,在普通登录教务系统后最终调用或通过KeyCode快速连接直接调用
     * UserLoginService内部调用的模块方法
     *
     * @param httpClient
     * @param system_identity
     * @param system_password
     * @param system_key
     * @param system_number
     * @return
     * @throws IOException
     * @throws ServerErrorException
     */
    private BaseResult<User, LoginResultEnum> CasVerify(CloseableHttpClient httpClient, String system_identity
            , String system_password, String system_key, String system_number) throws IOException, ServerErrorException {
        BaseResult<User, LoginResultEnum> result = new BaseResult<>();
        HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + system_identity + "&k=" + system_key);
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            httpGet = new HttpGet(url + "xs_main.aspx?xh=" + system_number);
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("正方教务管理系统")) {
                //获取学生的身份证号
                httpGet = new HttpGet(url + "xsgrxx.aspx?xh=" + system_number);
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //缓存学生的信息
                    User user = new User();
                    user.setUsername(system_identity);
                    user.setPassword(system_password);
                    user.setKeycode(system_key);
                    user.setNumber(system_number);
                    result.setResultData(user);
                    result.setResultType(LoginResultEnum.LOGIN_SUCCESS);
                    return result;
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 定时更新用户账号信息
     */
    @Scheduled(fixedDelay = 5000)
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
