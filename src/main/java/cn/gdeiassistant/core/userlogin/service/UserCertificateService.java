package cn.gdeiassistant.core.userlogin.service;

import org.springframework.beans.factory.annotation.Value;

import cn.gdeiassistant.common.spi.SessionCertificateProvider;
import cn.gdeiassistant.common.exception.commonexception.NetWorkTimeoutException;
import cn.gdeiassistant.common.exception.commonexception.PasswordIncorrectException;
import cn.gdeiassistant.common.exception.commonexception.ServerErrorException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.integration.httpclient.HttpClientSession;
import cn.gdeiassistant.common.pojo.entity.UserCertificateEntity;
import cn.gdeiassistant.common.redis.usercertificate.UserCertificateDao;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import cn.gdeiassistant.common.i18n.Messages;

@Service
public class UserCertificateService implements SessionCertificateProvider {

    private final String eduBaseUrl;

    private final HttpClientUtils httpClientUtils;

    private final Logger logger = LoggerFactory.getLogger(UserCertificateService.class);

    private final UserCertificateDao userCertificateDao;

    private final cn.gdeiassistant.core.user.mapper.UserMapper userMapper;

    /**
     * Account state is a userlogin concern. The servlet filter asks for a boolean
     * instead of holding a MyBatis mapper itself.
     */
    @Override
    public boolean isAccountActive(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        cn.gdeiassistant.core.user.pojo.entity.CampusAccountView account = userMapper.selectUser(username);
        return account != null && account.isActive();
    }

    private final String casLoginUrl;
    private final String portalLoginUrl;

        public UserCertificateService() {
        this(null, null, null, null, null, null);
    }

    public UserCertificateService(@Value("${campus.upstream.edu-base-url:http://jwgl.gdei.edu.cn}") String eduBaseUrl, HttpClientUtils httpClientUtils, UserCertificateDao userCertificateDao, cn.gdeiassistant.core.user.mapper.UserMapper userMapper, @Value("${campus.upstream.cas-login-url:https://security.gdei.edu.cn/cas/login}") String casLoginUrl, @Value("${campus.upstream.portal-login-url:http://portal.gdei.edu.cn:8001/Login}") String portalLoginUrl) {
        this.eduBaseUrl = eduBaseUrl;
        this.httpClientUtils = httpClientUtils;
        this.userCertificateDao = userCertificateDao;
        this.userMapper = userMapper;
        this.casLoginUrl = casLoginUrl;
        this.portalLoginUrl = portalLoginUrl;
    }

    /** A fresh isolated CAS exchange. Existing cookies and cached passwords cannot authorize sensitive actions. */
    public void verifyCurrentPassword(String username, String password) throws Exception {
        if (username == null || password == null || password.isBlank()) throw new PasswordIncorrectException(Messages.get("deletion.campusPasswordRequired"));
        HttpClientSession isolated = httpClientUtils.getHttpClient(null, false, 15);
        try (CloseableHttpClient client = isolated.getCloseableHttpClient()) {
            try (CloseableHttpResponse page = client.execute(new HttpGet(casLoginUrl))) {
                if (page.getStatusLine().getStatusCode() != 200) throw new ServerErrorException(Messages.get("userlogin.campusAuthUnavailable"));
                Document document = Jsoup.parse(EntityUtils.toString(page.getEntity()));
                Element tokens = document.getElementById("tokens");
                Element stamp = document.getElementById("stamp");
                if (tokens == null || stamp == null) throw new ServerErrorException(Messages.get("userlogin.campusAuthResponseInvalid"));
                HttpPost post = new HttpPost(casLoginUrl);
                post.setEntity(new UrlEncodedFormEntity(List.of(
                        new BasicNameValuePair("username", username), new BasicNameValuePair("password", password),
                        new BasicNameValuePair("service", portalLoginUrl), new BasicNameValuePair("tokens", tokens.val()),
                        new BasicNameValuePair("stamp", stamp.val()), new BasicNameValuePair("imageField.x", "0"),
                        new BasicNameValuePair("imageField.y", "0")), StandardCharsets.UTF_8));
                try (CloseableHttpResponse result = client.execute(post)) {
                    if (result.getStatusLine().getStatusCode() != 200) throw new ServerErrorException(Messages.get("userlogin.campusAuthUnavailable"));
                    Element body = Jsoup.parse(EntityUtils.toString(result.getEntity())).body();
                    if (!body.hasAttr("bgcolor")) throw new PasswordIncorrectException(Messages.get("grade.userPasswordIncorrect"));
                }
            }
        } catch (IOException failure) {
            throw new NetWorkTimeoutException(Messages.get("userlogin.campusAuthTimeout"));
        }
    }

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
     * 清除指定用户在 Redis 中已知的可复用校园凭证缓存。
     */
    public void clearReusableCredentials(String username) {
        if (username != null && !username.isEmpty()) {
            userCertificateDao.deleteUserSessionCertificatesByUsername(username);
            userCertificateDao.deleteUserLoginCertificatesByUsername(username);
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
        UserCertificateEntity userCertificate = userCertificateDao.queryUserSessionCertificate(sessionId);
        if (userCertificate == null) {
            CloseableHttpClient httpClient = null;
            CookieStore cookieStore = null;
            try {
                HttpClientSession httpClientSession = httpClientUtils.getHttpClient(sessionId
                        , false, 15);
                httpClient = httpClientSession.getCloseableHttpClient();
                cookieStore = httpClientSession.getCookieStore();
                HttpGet httpGet = new HttpGet(casLoginUrl);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpPost httpPost = new HttpPost(casLoginUrl);
                    //封装身份认证需要POST发送的相关数据
                    List<BasicNameValuePair> basicNameValuePairs = new ArrayList<>();
                    basicNameValuePairs.add(new BasicNameValuePair("username", username));
                    basicNameValuePairs.add(new BasicNameValuePair("password", password));
                    basicNameValuePairs.add(new BasicNameValuePair("service", portalLoginUrl));
                    basicNameValuePairs.add(new BasicNameValuePair("imageField.x", "0"));
                    basicNameValuePairs.add(new BasicNameValuePair("imageField.y", "0"));
                    Element tokensEl = document.getElementById("tokens");
                    Element stampEl = document.getElementById("stamp");
                    if (tokensEl == null || stampEl == null) {
                        throw new ServerErrorException(Messages.get("userlogin.casPageTokensMissing"));
                    }
                    basicNameValuePairs.add(new BasicNameValuePair("tokens", tokensEl.val()));
                    basicNameValuePairs.add(new BasicNameValuePair("stamp", stampEl.val()));
                    //绑定表单参数
                    httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairs, StandardCharsets.UTF_8));
                    httpResponse = httpClient.execute(httpPost);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() != 200) {
                        //服务器异常
                        throw new ServerErrorException(Messages.get("grade.eduSystemError"));
                    }
                    Element bodyElement = document.select("body").first();
                    if (bodyElement == null || !bodyElement.hasAttr("bgcolor")) {
                        //认证失败,提示账号或密码错误
                        throw new PasswordIncorrectException(Messages.get("userlogin.loginCredentialsIncorrect"));
                    }
                    Element firstLink = document.select("a").first();
                    if (firstLink == null) {
                        throw new ServerErrorException(Messages.get("userlogin.casRedirectMissing"));
                    }
                    String casRedirectUrl = firstLink.attr("href");
                    if (casRedirectUrl == null || casRedirectUrl.isEmpty()) {
                        throw new ServerErrorException(Messages.get("userlogin.casRedirectEmpty"));
                    }
                    // Validate redirect URL stays within trusted school domains
                    try {
                        java.net.URI uri = java.net.URI.create(casRedirectUrl);
                        String scheme = uri.getScheme();
                        if (scheme != null && !scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")) {
                            throw new ServerErrorException(Messages.get("userlogin.casRedirectProtocolUnsafe"));
                        }
                        String host = uri.getHost();
                        if (host != null && !host.toLowerCase().endsWith(".gdei.edu.cn") && !host.toLowerCase().equals("gdei.edu.cn")) {
                            throw new ServerErrorException(Messages.get("userlogin.casRedirectHostUntrusted"));
                        }
                    } catch (IllegalArgumentException e) {
                        throw new ServerErrorException(Messages.get("userlogin.casRedirectMalformed"));
                    }
                    httpGet = new HttpGet(casRedirectUrl);
                    httpResponse = httpClient.execute(httpGet);
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 302) {
                        if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                            //已经通过了认证
                            loginCasSystem(sessionId, httpClient, username, password);
                            return;
                        } else {
                            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                            httpResponse = httpClient.execute(httpGet);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("我的门户")) {
                                //登录我的门户成功
                                loginCasSystem(sessionId, httpClient, username, password);
                                return;
                            }
                        }
                    }
                    throw new ServerErrorException(Messages.get("grade.eduSystemError"));
                } else if (httpResponse.getStatusLine().getStatusCode() == 302) {
                    if ("newpages/b.html".equals(httpResponse.getFirstHeader("Location").getValue())) {
                        //已经通过了认证
                        loginCasSystem(sessionId, httpClient, username, password);
                        return;
                    }
                }
                throw new ServerErrorException(Messages.get("grade.eduSystemError"));
            } catch (PasswordIncorrectException ignored) {
                throw new PasswordIncorrectException(Messages.get("grade.userPasswordIncorrect"));
            } catch (IOException e) {
                logger.error("与教务系统同步会话时网络异常", e);
                throw new NetWorkTimeoutException(Messages.get("grade.networkTimeout"));
            } catch (Exception e) {
                logger.error("与教务系统同步会话时发生异常", e);
                throw new ServerErrorException(Messages.get("grade.eduSystemError"));
            } finally {
                if (httpClient != null) {
                    try {
                        httpClient.close();
                    } catch (IOException e) {
                        logger.error("关闭教务系统同步 HttpClient 失败", e);
                    }
                }
                if (cookieStore != null) {
                    httpClientUtils.syncHttpClientCookieStore(sessionId, cookieStore);
                }
            }
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
    private void loginCasSystem(String sessionId, CloseableHttpClient httpClient, String username, String password) throws Exception {
        HttpGet httpGet = new HttpGet(eduBaseUrl + "/login_cas.aspx");
        HttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 302) {
            httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
            httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            Element loginLink1 = document.select("a").first();
            if (loginLink1 == null) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectMissing"));
            }
            String loginRedirect1 = loginLink1.attr("href");
            if (loginRedirect1 == null || loginRedirect1.isEmpty()) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectEmpty"));
            }
            // Validate redirect URL stays within trusted school domains
            try {
                java.net.URI uri1 = java.net.URI.create(loginRedirect1);
                String scheme1 = uri1.getScheme();
                if (scheme1 != null && !scheme1.equalsIgnoreCase("http") && !scheme1.equalsIgnoreCase("https")) {
                    throw new ServerErrorException(Messages.get("userlogin.casRedirectProtocolUnsafe"));
                }
                String host1 = uri1.getHost();
                if (host1 != null && !host1.toLowerCase().endsWith(".gdei.edu.cn") && !host1.toLowerCase().equals("gdei.edu.cn")) {
                    throw new ServerErrorException(Messages.get("userlogin.casRedirectHostUntrusted"));
                }
            } catch (IllegalArgumentException e) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectMalformed"));
            }
            httpGet = new HttpGet(loginRedirect1);
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            String loginRedirect2 = document.select("a").attr("href");
            if (loginRedirect2 == null || loginRedirect2.isEmpty()) {
                throw new ServerErrorException(Messages.get("userlogin.casRedirectEmpty"));
            }
            httpGet = new HttpGet(eduBaseUrl + "/" + loginRedirect2);
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            //获取学生的教务系统信息
            Element scriptElement = document.select("script").first();
            if (scriptElement == null) {
                throw new ServerErrorException(Messages.get("userlogin.eduPageScriptMissing"));
            }
            String script = scriptElement.data();
            Element form1Element = document.getElementById("Form1");
            if (form1Element == null) {
                throw new ServerErrorException(Messages.get("userlogin.eduPageFormMissing"));
            }
            String keycode = (form1Element.attr("action")
                    .split("&")[1]).split("=")[1];
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d{10,12}");
            java.util.regex.Matcher matcher = pattern.matcher(script);
            if (!matcher.find()) {
                throw new ServerErrorException(Messages.get("userlogin.eduStudentIdMissing"));
            }
            String number = matcher.group();
            Long timestamp = Long.valueOf(form1Element
                    .attr("action").split("&")[2].split("=")[1]);
            //进行教务系统身份校验
            casVerify(sessionId, httpClient, username, password, keycode, number, timestamp);
            return;
        }
        throw new ServerErrorException(Messages.get("grade.eduSystemError"));
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
    private void casVerify(String sessionId, CloseableHttpClient httpClient, String username
            , String password, String keycode, String number, Long timestamp) throws Exception {
        HttpGet httpGet = new HttpGet(eduBaseUrl + "/cas_verify.aspx?i=" + username + "&k="
                + keycode + "&timestamp=" + timestamp);
        CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            /*if (document.select("script").size() > 0
                    && document.select("script").text().equals("alert('用户【工号】在教务系统中不存在。！');")) {
                throw new UserGraduatedException(Messages.get("userlogin.accountGraduated"));
            }*/
            httpResponse.close();
            httpGet = new HttpGet(eduBaseUrl + "/xs_main.aspx?xh=" + number + "&type=1");
            httpResponse = httpClient.execute(httpGet);
            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200
                    && "正方教务管理系统".equals(new String(document.title()
                    .getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8))) {
                //获取学生的身份证号
                httpGet = new HttpGet(eduBaseUrl + "/xsgrxx.aspx?xh=" + number);
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
                    return;
                }
                throw new ServerErrorException(Messages.get("grade.eduSystemError"));
            }
            throw new ServerErrorException(Messages.get("grade.eduSystemError"));
        }
        throw new ServerErrorException(Messages.get("grade.eduSystemError"));
    }

}
