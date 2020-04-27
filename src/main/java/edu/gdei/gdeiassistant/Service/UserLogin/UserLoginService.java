package edu.gdei.gdeiassistant.Service.UserLogin;

import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.UserNotExistException;
import edu.gdei.gdeiassistant.Exception.UserLoginException.UserGraduatedException;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.User.UserMapper;
import edu.gdei.gdeiassistant.Repository.Redis.UserCertificate.UserCertificateDao;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import edu.gdei.gdeiassistant.Tools.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

@Service
public class UserLoginService {

    private Logger logger = LoggerFactory.getLogger(UserLoginService.class);

    private int timeout;

    private String url;

    @Resource(name = "userMapper")
    private UserMapper userMapper;

    @Autowired
    private AsyncRestTemplate asyncRestTemplate;

    @Autowired
    private UserCertificateDao userCertificateDao;

    @Value("#{propertiesReader['education.system.url']}")
    public void setUrl(String url) {
        this.url = url;
    }


    @Value("#{propertiesReader['timeout.userlogin']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 查询数据库中用户名对应的用户账号信息
     *
     * @param username
     * @return
     */
    public User GetUserByUsername(String username) throws Exception {
        User user = userMapper.selectUser(StringEncryptUtils.encryptString(username));
        if (user != null) {
            return user.decryptUser();
        }
        throw new UserNotExistException("当前用户不存在");
    }

    /**
     * 用户登录学院系统业务的接口
     *
     * @param sessionId
     * @param user
     * @param quickLogin
     * @return
     */
    public UserCertificate UserLogin(String sessionId, User user, boolean quickLogin) throws Exception {
        //查询数据库,若账号密码相同,则直接通过登录校验
        if (quickLogin) {
            try {
                User queryUser = userMapper.selectUser(StringEncryptUtils.encryptString(user.getUsername()));
                if (queryUser != null && !queryUser.getState().equals(2)) {
                    //将数据库查询的用户数据与用户提交的用户信息进行对比
                    User decryptUser = queryUser.decryptUser();
                    if (StringUtils.isNotBlank(user.getPassword())) {
                        if (decryptUser.getUsername().equals(user.getUsername())
                                && decryptUser.getPassword().equals(user.getPassword())) {
                            //登录成功,缓存并返回用户信息
                            UserCertificate userCertificate = new UserCertificate();
                            userCertificate.setUser(queryUser.decryptUser());
                            return userCertificate;
                        }
                    }
                }
            } catch (Exception e) {
                //若查询数据库或加解密出现异常,则放弃数据库校验,使用学校教务系统登录校验
                logger.error("用户登录数据库校验异常：", e);
            }
        }
        //用户不存在或与数据库的数据信息不匹配,进行普通登录
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                    , false, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
                //封装身份认证需要POST发送的相关数据
                List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                basicNameValuePairs.add(new BasicNameValuePair("username", user.getUsername()));
                basicNameValuePairs.add(new BasicNameValuePair("password", user.getPassword()));
                basicNameValuePairs.add(new BasicNameValuePair("service", "http://portal.gdei.edu.cn:8001/Login"));
                basicNameValuePairs.add(new BasicNameValuePair("imageField.x", "0"));
                basicNameValuePairs.add(new BasicNameValuePair("imageField.y", "0"));
                basicNameValuePairs.add(new BasicNameValuePair("tokens", document.getElementById("tokens").val()));
                basicNameValuePairs.add(new BasicNameValuePair("stamp", document.getElementById("stamp").val()));
                //绑定表单参数
                httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                httpResponse = httpClient.execute(httpPost);
                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
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
                if (httpResponse.getStatusLine().getStatusCode() == 302) {
                    if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                        //已经通过了认证
                        return LoginCasSystem(httpClient, user);
                    } else {
                        httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("我的门户")) {
                            //登录我的门户成功
                            return LoginCasSystem(httpClient, user);
                        }
                    }
                }
                throw new ServerErrorException("教务系统异常");
            } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                    //已经通过了认证
                    return LoginCasSystem(httpClient, user);
                }
            }
            throw new ServerErrorException("教务系统异常");
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("用户密码错误");
        } catch (IOException e) {
            logger.error("用户登录异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (UserGraduatedException e) {
            logger.error("用户登录异常：", e);
            throw new UserGraduatedException("用户账号已毕业注销");
        } catch (Exception e) {
            logger.error("用户登录异常：", e);
            throw new ServerErrorException("教务系统异常");
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (cookieStore != null) {
                HttpClientUtils.SyncHttpClientCookieStore(sessionId, cookieStore);
            }
        }
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
    private UserCertificate LoginCasSystem(CloseableHttpClient httpClient, User user) throws Exception {
        HttpGet httpGet = new HttpGet(url + "login_cas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 302) {
            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
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
    private UserCertificate CasVerify(CloseableHttpClient httpClient, String username
            , String password, String keycode, String number, Long timestamp) throws Exception {
        HttpGet httpGet = new HttpGet(url + "cas_verify.aspx?i=" + username + "&k="
                + keycode + "&timestamp=" + timestamp);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (document.select("script").size() > 0
                    && document.select("script").text().equals("alert('用户【工号】在教务系统中不存在。！');")) {
                throw new UserGraduatedException("该账号已毕业注销");
            }
            httpResponse.close();
            httpGet = new HttpGet(url + "xs_main.aspx?xh=" + number + "&type=1");
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
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
                    user.setGroup(UserGroupEnum.STUDENT.getValue());
                    UserCertificate userCertificate = new UserCertificate();
                    userCertificate.setUser(user);
                    userCertificate.setTimestamp(timestamp);
                    return userCertificate;
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 同步地与教务系统会话进行同步
     *
     * @param sessionId
     * @param user
     * @return
     * @throws Exception
     */
    public UserCertificate SyncUpdateSession(String sessionId, User user) throws Exception {
        UserCertificate userCertificate = UserLogin(sessionId, user, false);
        Long timestamp = userCertificate.getTimestamp();
        if (StringUtils.isBlank(user.getKeycode())) {
            user.setKeycode(userCertificate.getUser().getKeycode());
        }
        if (StringUtils.isBlank(user.getNumber())) {
            user.setNumber(userCertificate.getUser().getNumber());
        }
        userCertificate = new UserCertificate();
        userCertificate.setUser(user);
        userCertificate.setTimestamp(timestamp);
        userCertificateDao.saveUserCertificate(userCertificate);
        return userCertificate;
    }

    /**
     * 异步地与教务系统会话进行同步
     *
     * @param request
     */
    @Async
    public void AsyncUpdateSession(HttpServletRequest request) throws Exception {
        HttpSession httpSession = request.getSession();
        String username = (String) httpSession.getAttribute("username");
        String password = (String) httpSession.getAttribute("password");
        UserCertificate userCertificate = userCertificateDao.queryUserCertificate(username);
        if (userCertificate == null) {
            User user = new User(username, password);
            userCertificate = UserLogin(request.getSession().getId(), user, false);
            userCertificateDao.saveUserCertificate(userCertificate);
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
                        logger.error("定时更新用户账号信息异常：", ex);
                        semaphore.release();
                    }

                    @Override
                    public void onSuccess(ResponseEntity<DataJsonResult<User>> result) {
                        semaphore.release();
                    }
                });
            }
        } catch (Exception e) {
            logger.error("定时更新用户账号信息异常：", e);
        }
    }
}
