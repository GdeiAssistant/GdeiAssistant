package edu.gdei.gdeiassistant.Service.BookQuery;

import edu.gdei.gdeiassistant.Exception.BookRenewException.BookRenewOvertimeException;
import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Pojo.Entity.Book;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Pojo.UserLogin.UserCertificate;
import edu.gdei.gdeiassistant.Repository.Redis.UserCertificate.UserCertificateDao;
import edu.gdei.gdeiassistant.Service.UserLogin.UserLoginService;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import net.sf.json.JSONObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookQueryService {

    @Autowired
    private UserCertificateDao userCertificateDao;

    @Autowired
    private UserLoginService userLoginService;

    private Logger logger = LoggerFactory.getLogger(BookQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.bookquery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 同步教务系统会话，获取用户手机号
     *
     * @param sessionId
     * @param username
     * @param password
     * @return
     * @throws Exception
     */
    public String UpdateSessionAndGetUserNumber(String sessionId, String username, String password) throws Exception {
        UserCertificate userCertificate = userCertificateDao.queryUserCertificate(username);
        if (userCertificate == null) {
            userCertificate = userLoginService.SyncUpdateSession(sessionId, new User(username, password));
        }
        return userCertificate.getUser().getNumber();
    }

    /**
     * 续借图书
     *
     * @param sessionId
     * @param sn
     * @param code
     * @throws NetWorkTimeoutException
     * @throws ServerErrorException
     */
    public void BookRenew(String sessionId, String sn, String code) throws NetWorkTimeoutException, ServerErrorException, BookRenewOvertimeException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet("http://agentdockingopac.featurelib.libsou.com/showhome/searchrenew/opacSearchRenew?&check=1&sn="
                    + sn + "&code=" + code + "&schoolId=705");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                JSONObject jsonObject = JSONObject.fromObject(EntityUtils.toString(httpResponse.getEntity()));
                int result = jsonObject.getInt("result");
                String message = jsonObject.getString("msg");
                if (result == 1) {
                    if (message.equals("超过最大续借次数:1！")) {
                        throw new BookRenewOvertimeException("图书续借超过次数限制");
                    }
                    return;
                }
            }
            throw new ServerErrorException("图书馆系统异常");
        } catch (IOException e) {
            logger.error("续借图书异常:", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (BookRenewOvertimeException e) {
            logger.error("续借图书异常:", e);
            throw new BookRenewOvertimeException("图书续借超过次数限制");
        } catch (Exception e) {
            logger.error("续借图书异常", e);
            throw new ServerErrorException("图书馆系统异常");
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
     * 查询借阅图书
     *
     * @param sessionId
     * @param number
     * @param password
     * @return
     * @throws NetWorkTimeoutException
     * @throws ServerErrorException
     * @throws PasswordIncorrectException
     */
    public List<Book> BookQuery(String sessionId, String number, String password) throws NetWorkTimeoutException, ServerErrorException, PasswordIncorrectException {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //进入移动图书馆主页
            HttpGet httpGet = new HttpGet("http://m.5read.com/705");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                //进入移动图书馆登录界面
                httpGet = new HttpGet("http://mc.m.5read.com/user/login/showLogin.jspx?backurl=/user/uc/showUserCenter.jspx");
                httpResponse = httpClient.execute(httpGet);
                Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                if (httpResponse.getStatusLine().getStatusCode() == 200
                        && document.title().equals("移动图书馆服务登录")) {
                    //进行账号登录
                    HttpPost httpPost = new HttpPost("http://mc.m.5read.com/irdUser/login/opac/opacLogin.jspx");
                    List<BasicNameValuePair> basicNameValuePairList = new ArrayList<>();
                    basicNameValuePairList.add(new BasicNameValuePair("schoolid", "705"));
                    basicNameValuePairList.add(new BasicNameValuePair("backurl", "/user/uc/showUserCenter.jspx"));
                    basicNameValuePairList.add(new BasicNameValuePair("userType", "0"));
                    basicNameValuePairList.add(new BasicNameValuePair("username", number));
                    basicNameValuePairList.add(new BasicNameValuePair("password", password));
                    httpPost.setEntity(new UrlEncodedFormEntity(basicNameValuePairList, StandardCharsets.UTF_8));
                    httpResponse = httpClient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == 302) {
                        httpGet = new HttpGet(httpResponse.getFirstHeader("Location").getValue());
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("个人中心")) {
                            httpGet = new HttpGet("http://mc.m.5read.com/cmpt/opac/opacLink.jspx?stype=1");
                            httpResponse = httpClient.execute(httpGet);
                            document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                Elements books = document.getElementsByClass("tableLib");
                                List<Book> bookList = new ArrayList<>();
                                for (Element element : books) {
                                    //解析SN号、Code号
                                    String[] renew = element.getElementsByClass("tableCon").first().select("a")
                                            .first().attr("onclick").split("','");
                                    String sn = renew[0].split("renew\\('")[1];
                                    String code = renew[1].split("', '")[0];
                                    //获取条形码
                                    element.select("td").first().select("a").remove();
                                    String id = element.select("td").first().text();
                                    //获取书名
                                    String name = element.select("td").get(1).text();
                                    //获取作者名
                                    String author = element.select("td").get(2).text();
                                    //获取借阅时间
                                    String borrowTime = element.select("td").get(3).text();
                                    //获取应还日期
                                    String returnTime = element.select("td").get(4).text();
                                    //获取续借次数
                                    Integer renewTime = Integer.valueOf(element.select("td").get(5).text());
                                    //构造Book对象
                                    Book book = new Book();
                                    book.setId(id);
                                    book.setName(name);
                                    book.setAuthor(author);
                                    book.setBorrowDate(borrowTime);
                                    book.setReturnDate(returnTime);
                                    book.setRenewTime(renewTime);
                                    book.setSn(sn);
                                    book.setCode(code);
                                    bookList.add(book);
                                }
                                return bookList;
                            }
                        } else if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("移动图书馆服务登录")) {
                            throw new PasswordIncorrectException("图书馆查询密码错误");
                        }
                    }
                }
            }
            throw new ServerErrorException("图书馆系统异常");
        } catch (IOException e) {
            logger.error("查询借阅图书异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("图书馆查询密码错误");
        } catch (Exception e) {
            logger.error("查询借阅图书异常：", e);
            throw new ServerErrorException("图书馆系统异常");
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
}
