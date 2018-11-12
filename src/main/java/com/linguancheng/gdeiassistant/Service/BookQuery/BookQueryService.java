package com.linguancheng.gdeiassistant.Service.BookQuery;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import com.linguancheng.gdeiassistant.Exception.CommonException.ServerErrorException;
import com.linguancheng.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import com.linguancheng.gdeiassistant.Tools.HttpClientUtils;
import com.linguancheng.gdeiassistant.Pojo.Entity.Book;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by linguancheng on 2017/7/22.
 */

@Service
public class BookQueryService {

    private Log log = LogFactory.getLog(BookQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.bookquery']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 续借图书
     *
     * @param sessionId
     * @param url
     * @return
     */
    public BaseResult<String, ServiceResultEnum> BookRenew(String sessionId, String url) {
        BaseResult<String, ServiceResultEnum> result = new BaseResult<>();
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpPost httpPost = new HttpPost("http://lib2.gdei.edu.cn:8080/sms/opac/user/" + url);
            HttpResponse httpResponse = httpClient.execute(httpPost);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("个人借阅信息")) {
                String message = document.getElementsByClass("sheet").first().select("font").first().text();
                result.setResultData(message);
                result.setResultType(ServiceResultEnum.SUCCESS);
                return result;
            }
            throw new ServerErrorException("图书馆系统异常");
        } catch (IOException e) {
            log.error("续借图书异常:", e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (Exception e) {
            log.error("续借图书异常", e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
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
        return result;
    }

    /**
     * 查询借阅图书
     *
     * @param sessionId
     * @param number
     * @param password
     * @return
     */
    public BaseResult<List<Book>, ServiceResultEnum> BookQuery(String sessionId
            , String number, String password) {
        BaseResult<List<Book>, ServiceResultEnum> result = new BaseResult<>();
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
                httpGet = new HttpGet("http://mc.m.5read.com/user/uc/showUserCenter.jspx");
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
                    document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                    if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("移动图书馆服务登录")) {
                        //登录失败，检查失败原因
                        String error = document.getElementsByClass("uinfo").first().select("em").first().text();
                        switch (error) {
                            case "您的口令与证号相同，请先修改口令!":
                                //初始密码未修改
                                break;

                            case "口令错误!":
                                throw new PasswordIncorrectException("借阅证密码不正确");

                            default:
                                throw new ServerErrorException("图书馆系统异常");
                        }
                    } else if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("个人中心")) {
                        //登录成功，查询借阅的图书信息
                        httpGet = new HttpGet("http://mc.m.5read.com/cmpt/opac/opacLink.jspx?stype=1");
                        httpResponse = httpClient.execute(httpGet);
                        document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
                        if (httpResponse.getStatusLine().getStatusCode() == 200 && document.title().equals("个人借阅信息")) {
                            Elements sheets = document.getElementsByClass("sheet");
                            if (sheets.size() == 0) {
                                result.setResultType(ServiceResultEnum.EMPTY_RESULT);
                                return result;
                            }
                            List<Book> bookList = new ArrayList<>();
                            for (Element sheet : sheets) {
                                //获取所有行
                                Elements trs = sheet.select("tr");
                                //获取图书名
                                String name = trs.get(0).select("td").get(0).text();
                                //获取登录号
                                String id = trs.get(1).select("td").get(0).text();
                                //获取借阅时间
                                String borrowDate = trs.get(2).select("td").get(0).text();
                                //获取归还时间
                                String returnDate = trs.get(3).select("td").get(0).text();
                                //获取图书类型
                                String type = trs.get(4).select("td").get(0).text();
                                //获取续借地址
                                String renewUrl = sheet.select("form").first().attr("action");
                                //保存图书信息到List中
                                Book book = new Book();
                                book.setName(name);
                                book.setId(id);
                                book.setBorrowDate(borrowDate);
                                book.setReturnDate(returnDate);
                                book.setType(type);
                                book.setRenewUrl(renewUrl);
                                bookList.add(book);
                            }
                            result.setResultData(bookList);
                            result.setResultType(ServiceResultEnum.SUCCESS);
                            return result;
                        }
                        throw new ServerErrorException("图书馆系统异常");
                    }
                    throw new ServerErrorException("图书馆系统异常");
                }
                throw new ServerErrorException("图书馆系统异常");
            }
        } catch (IOException e) {
            log.error("查询借阅图书异常：", e);
            result.setResultType(ServiceResultEnum.TIME_OUT);
        } catch (PasswordIncorrectException e) {
            log.error("查询借阅图书异常：", e);
            result.setResultType(ServiceResultEnum.PASSWORD_INCORRECT);
        } catch (Exception e) {
            log.error("查询借阅图书异常：", e);
            result.setResultType(ServiceResultEnum.SERVER_ERROR);
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
        return result;
    }
}
