package cn.gdeiassistant.core.userLogin.service;

import cn.gdeiassistant.common.config.properties.TrialProperties;
import cn.gdeiassistant.common.exception.CommonException.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.CommonException.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.CommonException.ServerErrorException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.core.userLogin.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.redis.UserCertificate.UserCertificateDao;
import cn.gdeiassistant.integration.httpclient.HttpClientUtils;
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
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class UserCertificateService {

    private final Logger logger = LoggerFactory.getLogger(UserCertificateService.class);

    @Autowired
    private TrialProperties trialProperties;

    @Autowired
    private UserCertificateDao userCertificateDao;

    /**
     * 获取用户Cookie凭证
     *
     * @param cookieId
     * @return
     */
    public User getUserCookieCertificate(String cookieId) {
        return userCertificateDao.queryUserCookieCertificate(cookieId);
    }

    /**
     * 保存用户登录凭证
     *
     * @param cookieId
     * @param username
     * @param password
     */
    public void saveUserCookieCertificate(String cookieId, String username, String password) {
        userCertificateDao.saveUserCookieCertificate(cookieId, username, password);
    }

    /**
     * 更新用户登录凭证有效期
     *
     * @param cookieId
     */
    public void updateUserCookieExpiration(String cookieId) {
        userCertificateDao.updateUserCookieCertificateExpiration(cookieId);
    }

    /**
     * 获取用户登录凭证
     *
     * @param sessionId
     * @return
     */
    public User getUserLoginCertificate(String sessionId) {
        return userCertificateDao.queryUserLoginCertificate(sessionId);
    }

    /**
     * 保存用户登录凭证
     *
     * @param sessionId
     * @param username
     * @param password
     */
    public void saveUserLoginCertificate(String sessionId, String username, String password) {
        userCertificateDao.saveUserLoginCertificate(sessionId, username, password);
    }

    /**
     * 更新用户登录凭证有效期
     *
     * @param sessionId
     */
    public void updateUserLoginExpiration(String sessionId) {
        userCertificateDao.updateUserLoginCertificateExpiration(sessionId);
    }

    /**
     * 退出登录：清除该 sessionId 在 Redis 中的登录凭证与教务会话凭证，使对应 JWT 事实失效。
     *
     * @param sessionId
     */
    public void clearUserLoginAndSession(String sessionId) {
        if (sessionId != null && !sessionId.isEmpty()) {
            userCertificateDao.deleteUserSessionCertificate(sessionId);
            userCertificateDao.deleteUserLoginCertificate(sessionId);
        }
    }

    /**
     * 获取缓存的教务系统会话凭证
     *
     * @param sessionId
     * @return
     */
    public UserCertificateEntity getUserSessionCertificate(String sessionId) throws NetWorkTimeoutException, PasswordIncorrectException, ServerErrorException {
        UserCertificateEntity userCertificate = userCertificateDao.queryUserSessionCertificate(sessionId);
        if (userCertificate == null) {
            User user = getUserLoginCertificate(sessionId);
            updateSessionCertificate(sessionId, user.getUsername(), user.getPassword());
            userCertificate = userCertificateDao.queryUserSessionCertificate(sessionId);
        }
        return userCertificate;
    }

    /**
     * 同步地与教务系统会话进行同步
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public void syncUpdateSessionCertificate(String sessionId, String username, String password) throws Exception {
        updateSessionCertificate(sessionId, username, password);
    }

    /**
     * 异步地与教务系统会话进行同步
     *
     * @param sessionId
     * @param user
     * @throws Exception
     */
    @Async
    public void asyncUpdateSessionCertificate(String sessionId, User user) throws Exception {
        updateSessionCertificate(sessionId, user.getUsername(), user.getPassword());
    }

    /**
     * 同步教务系统会话
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     */
    private void updateSessionCertificate(String sessionId, String username, String password) throws PasswordIncorrectException, NetWorkTimeoutException, ServerErrorException {
        // 仅对非测试账号执行真实 CAS 会话更新；测试账号（白名单）跳过爬虫
        if (!isTestAccount(username)) {
            UserCertificateEntity userCertificate = userCertificateDao.queryUserSessionCertificate(username);
            if (userCertificate == null) {
                CloseableHttpClient httpClient = null;
                CookieStore cookieStore = null;
                try {
                    HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId
                            , false, 15);
                    httpClient = httpClientSession.getCloseableHttpClient();
                    cookieStore = httpClientSession.getCookieStore();
                    HttpGet httpGet = new HttpGet("https://security.gdei.edu.cn/cas/login");
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpPost httpPost = new HttpPost("https://security.gdei.edu.cn/cas/login");
                        //封装身份认证需要POST发送的相关数据
                        List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                        basicNameValuePairs.add(new BasicNameValuePair("username", username));
                        basicNameValuePairs.add(new BasicNameValuePair("password", password));
                        basicNameValuePairs.add(new BasicNameValuePair("service", "http://portal.gdei.edu.cn:8001/Login"));
                        basicNameValuePairs.add(new BasicNameValuePair("imageField.x", "0"));
                        basicNameValuePairs.add(new BasicNameValuePair("imageField.y", "0"));
                        Element tokensEl = document.getElementById("tokens");
                        Element stampEl = document.getElementById("stamp");
                        if (tokensEl == null || stampEl == null) {
                            throw new ServerErrorException("CAS 登录页面结构异常，未找到 tokens 或 stamp");
                        }
                        basicNameValuePairs.add(new BasicNameValuePair("tokens", tokensEl.val()));
                        basicNameValuePairs.add(new BasicNameValuePair("stamp", stampEl.val()));
                        //绑定表单参数
                        httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                        httpResponse = httpClient.execute(httpPost);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() != 200) {
                            //服务器异常
                            throw new ServerErrorException("教务系统异常");
                        }
                        Element bodyElement = document.select("body").first();
                        if (bodyElement == null || !bodyElement.hasAttr("bgcolor")) {
                            //认证失败,提示账号或密码错误
                            throw new PasswordIncorrectException("登录账号密码不正确");
                        }
                        Element firstLink = document.select("a").first();
                        if (firstLink == null) {
                            throw new ServerErrorException("CAS 登录后未找到跳转链接");
                        }
                        String casRedirectUrl = firstLink.attr("href");
                        if (casRedirectUrl == null || casRedirectUrl.isEmpty()) {
                            throw new ServerErrorException("CAS login后跳转链接为空");
                        }
                        // Validate redirect URL stays within trusted school domains
                        try {
                            java.net.URI uri = java.net.URI.create(casRedirectUrl);
                            String host = uri.getHost();
                            if (host != null && !host.toLowerCase().endsWith(".gdei.edu.cn") && !host.toLowerCase().equals("gdei.edu.cn")) {
                                throw new ServerErrorException("CAS重定向地址不在可信域名范围内");
                            }
                        } catch (IllegalArgumentException e) {
                            throw new ServerErrorException("CAS重定向地址格式异常");
                        }
                        httpGet = new HttpGet(casRedirectUrl);
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 302) {
                            if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                                //已经通过了认证
                                LoginCasSystem(sessionId, httpClient, username, password);
                            } else {
                                httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                                httpResponse = httpClient.execute(httpGet);
                                document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                                if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("我的门户")) {
                                    //登录我的门户成功
                                    LoginCasSystem(sessionId, httpClient, username, password);
                                }
                            }
                        }
                        throw new ServerErrorException("教务系统异常");
                    } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                        if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                            //已经通过了认证
                            LoginCasSystem(sessionId, httpClient, username, password);
                        }
                    }
                    throw new ServerErrorException("教务系统异常");
                } catch (PasswordIncorrectException ignored) {
                    throw new PasswordIncorrectException("用户密码错误");
                } catch (IOException e) {
                    logger.error("与教务系统同步会话时网络异常", e);
                    throw new NetWorkTimeoutException("网络连接超时");
                } catch (Exception e) {
                    logger.error("与教务系统同步会话时发生异常", e);
                    throw new ServerErrorException("教务系统异常");
                } finally {
                    if (httpClient != null) {
                        try {
                            httpClient.close();
                        } catch (IOException e) {
                            logger.error("关闭教务系统同步 HttpClient 失败", e);
                        }
                    }
                    if (cookieStore != null) {
                        HttpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
                    }
                }
            }
        } else {
            //模拟登录环境下不允许修改密码
            throw new PasswordIncorrectException("登录账号密码不正确");
        }
    }

    /**
     * 登录教务系统,UserLoginService内部调用的模块方法
     *
     * @param httpClient
     * @param username
     * @param password
     * @return
     * @throws IOException
     * @throws ServerErrorException
     */
    private void LoginCasSystem(String sessionId, CloseableHttpClient httpClient, String username, String password) throws Exception {
        HttpGet httpGet = new HttpGet("http://jwgl.gdei.edu.cn/login_cas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 302) {
            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            Element loginLink1 = document.select("a").first();
            if (loginLink1 == null) {
                throw new ServerErrorException("CAS 登录后未找到跳转链接");
            }
            String loginRedirect1 = loginLink1.attr("href");
            if (loginRedirect1 == null || loginRedirect1.isEmpty()) {
                throw new ServerErrorException("CAS login后跳转链接为空");
            }
            // Validate redirect URL stays within trusted school domains
            try {
                java.net.URI uri1 = java.net.URI.create(loginRedirect1);
                String host1 = uri1.getHost();
                if (host1 != null && !host1.toLowerCase().endsWith(".gdei.edu.cn") && !host1.toLowerCase().equals("gdei.edu.cn")) {
                    throw new ServerErrorException("CAS重定向地址不在可信域名范围内");
                }
            } catch (IllegalArgumentException e) {
                throw new ServerErrorException("CAS重定向地址格式异常");
            }
            httpGet = new HttpGet(loginRedirect1);
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            String loginRedirect2 = document.select("a").attr("href");
            if (loginRedirect2 == null || loginRedirect2.isEmpty()) {
                throw new ServerErrorException("CAS login后跳转链接为空");
            }
            httpGet = new HttpGet("http://jwgl.gdei.edu.cn/" + loginRedirect2);
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            //获取学生的教务系统信息
            Element scriptElement = document.select("script").first();
            if (scriptElement == null) {
                throw new ServerErrorException("教务系统页面结构异常，未找到脚本元素");
            }
            String script = scriptElement.data();
            Element form1Element = document.getElementById("Form1");
            if (form1Element == null) {
                throw new ServerErrorException("教务系统页面结构异常，未找到 Form1");
            }
            String keycode = (form1Element.attr("action")
                    .split("&")[1]).split("=")[1];
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{10,12}");
            java.util.regex.Matcher matcher = pattern.matcher(script);
            if (!matcher.find()) {
                throw new ServerErrorException("无法从教务系统响应中提取学号");
            }
            String number = matcher.group();
            Long timestamp = Long.valueOf(form1Element
                    .attr("action").split("&")[2].split("=")[1]);
            //进行教务系统身份校验
            CasVerify(sessionId, httpClient, username, password, keycode, number, timestamp);
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
    private void CasVerify(String sessionId, CloseableHttpClient httpClient, String username
            , String password, String keycode, String number, Long timestamp) throws Exception {
        HttpGet httpGet = new HttpGet("http://jwgl.gdei.edu.cn/cas_verify.aspx?i=" + username + "&k="
                + keycode + "&timestamp=" + timestamp);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            /*if (document.select("script").size() > 0
                    && document.select("script").text().equals("alert('用户【工号】在教务系统中不存在。！');")) {
                throw new UserGraduatedException("该账号已毕业注销");
            }*/
            httpResponse.close();
            httpGet = new HttpGet("http://jwgl.gdei.edu.cn/xs_main.aspx?xh=" + number + "&type=1");
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200
                    && "正方教务管理系统".equals(new String(document.title()
                    .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))) {
                //获取学生的身份证号
                httpGet = new HttpGet("http://jwgl.gdei.edu.cn/xsgrxx.aspx?xh=" + number);
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    //缓存学生的信息
                    User user = new User();
                    user.setUsername(username);
                    user.setPassword(password);
                    UserCertificateEntity entity = new UserCertificateEntity();
                    entity.setUser(user);
                    entity.setKeycode(keycode);
                    entity.setNumber(number);
                    entity.setTimestamp(timestamp);
                    userCertificateDao.saveUserSessionCertificate(sessionId, entity);
                }
                throw new ServerErrorException("教务系统异常");
            }
            throw new ServerErrorException("教务系统异常");
        }
        throw new ServerErrorException("教务系统异常");
    }

    /**
     * 判断用户名是否位于配置的测试账号白名单中（与 TrialDataAspect/TrialProperties 使用同一配置 trial.test-accounts）。
     */
    private boolean isTestAccount(String username) {
        if (username == null) {
            return false;
        }
        List<String> testAccounts = trialProperties.getTestAccounts();
        if (testAccounts == null) {
            return false;
        }
        return testAccounts.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .anyMatch(s -> s.equalsIgnoreCase(username));
    }
}
