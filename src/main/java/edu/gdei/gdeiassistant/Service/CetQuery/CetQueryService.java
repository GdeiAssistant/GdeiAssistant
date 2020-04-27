package edu.gdei.gdeiassistant.Service.CetQuery;

import edu.gdei.gdeiassistant.Exception.CommonException.NetWorkTimeoutException;
import edu.gdei.gdeiassistant.Exception.CommonException.PasswordIncorrectException;
import edu.gdei.gdeiassistant.Exception.CommonException.ServerErrorException;
import edu.gdei.gdeiassistant.Exception.QueryException.ErrorQueryConditionException;
import edu.gdei.gdeiassistant.Pojo.CetQuery.CetQuery;
import edu.gdei.gdeiassistant.Pojo.Entity.Cet;
import edu.gdei.gdeiassistant.Pojo.Entity.CetNumber;
import edu.gdei.gdeiassistant.Pojo.HttpClient.HttpClientSession;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Cet.CetMapper;
import edu.gdei.gdeiassistant.Tools.HttpClientUtils;
import edu.gdei.gdeiassistant.Tools.ImageEncodeUtils;
import edu.gdei.gdeiassistant.Tools.StringEncryptUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;

@Service
public class CetQueryService {

    @Resource(name = "cetMapper")
    private CetMapper cetMapper;

    private Logger logger = LoggerFactory.getLogger(CetQueryService.class);

    private int timeout;

    @Value("#{propertiesReader['timeout.cet']}")
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * 进入学信网四六级成绩查询页面，获取验证码
     *
     * @param sessionId
     * @return
     */
    public String CetIndex(String sessionId) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            HttpGet httpGet = new HttpGet("http://www.chsi.com.cn/cet/");
            httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                httpGet = new HttpGet("http://www.chsi.com.cn/cet/ValidatorIMG.JPG");
                httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
                httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
                httpResponse = httpClient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    return ImageEncodeUtils.ConvertToBase64(httpResponse.getEntity().getContent());
                }
            }
            throw new ServerErrorException("访问学信网异常");
        } catch (IOException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询四六级成绩异常：", e);
            throw new ServerErrorException("学信网系统异常");
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
     * 查询四六级考试成绩
     *
     * @param sessionId
     * @param cetQuery
     * @return
     */
    public Cet CetQuery(String sessionId, CetQuery cetQuery) throws Exception {
        CloseableHttpClient httpClient = null;
        CookieStore cookieStore = null;
        try {
            HttpClientSession httpClientSession = HttpClientUtils.getHttpClient(sessionId, true, timeout);
            httpClient = httpClientSession.getCloseableHttpClient();
            cookieStore = httpClientSession.getCookieStore();
            //查询CET成绩信息
            HttpGet httpGet = new HttpGet("http://www.chsi.com.cn/cet/query?zkzh=" + cetQuery.getNumber() + "&xm=" + cetQuery.getName() + "&yzm=" + cetQuery.getCheckcode());
            httpGet.setHeader("Referer", "http://www.chsi.com.cn/cet/");
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36");
            HttpResponse httpResponse = httpClient.execute(httpGet);
            Document document = Jsoup.parse(EntityUtils.toString(httpResponse.getEntity()));
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                if (document.getElementsByClass("cetTable").size() == 0) {
                    if (document.getElementsByClass("error alignC marginT20").size() != 0) {
                        //准考证号或姓名错误
                        throw new PasswordIncorrectException("四六级查询信息错误");
                    }
                    if (document.getElementsByClass("error alignC").size() != 0) {
                        //验证码错误
                        throw new ErrorQueryConditionException("验证码信息错误");
                    }
                    throw new ServerErrorException("四六级查询系统异常");
                }
                //信息输入正确,进行信息解析
                //获取存放成绩的表格
                Element element = document.getElementsByClass("cetTable").get(0);
                //得到所有的行
                Elements trs = element.getElementsByTag("tr");
                //获取信息
                String name = trs.get(0).getElementsByTag("td").text();
                String school = trs.get(1).getElementsByTag("td").text();
                String type = trs.get(2).getElementsByTag("td").text();
                String admissionCard = trs.get(4).getElementsByTag("td").text();
                //获取总分信息,总分信息含有空格,需要将字符串中的空格删除,听力/阅读/写作与翻译的分数数据同理
                String totalScore = trs.get(5).getElementsByClass("colorRed").get(0).text().replace(" ", "");
                //获取听力分数
                String listeningScore = trs.get(6).select("td").get(1).text().replace(" ", "");
                //获取阅读分数
                String readingScore = trs.get(7).select("td").get(1).text().replace(" ", "");
                //获取写作与翻译分数
                String writingAndTranslatingScore = trs.get(8).select("td").get(1).text().replace(" ", "");
                //保存查询成绩结果并返回
                Cet cet = new Cet();
                cet.setName(name);
                cet.setSchool(school);
                cet.setType(type);
                cet.setAdmissionCard(admissionCard);
                cet.setTotalScore(totalScore);
                cet.setListeningScore(listeningScore);
                cet.setReadingScore(readingScore);
                cet.setWritingAndTranslatingScore(writingAndTranslatingScore);
                return cet;
            }
            throw new ServerErrorException("学信网系统异常");
        } catch (PasswordIncorrectException ignored) {
            throw new PasswordIncorrectException("账户密码错误");
        } catch (ErrorQueryConditionException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new ErrorQueryConditionException("查询条件错误");
        } catch (IOException e) {
            logger.error("查询四六级成绩异常：", e);
            throw new NetWorkTimeoutException("网络连接超时");
        } catch (Exception e) {
            logger.error("查询四六级成绩异常：", e);
            throw new ServerErrorException("学信网系统异常");
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
     * 查询保存的四六级准考证号
     *
     * @param username
     * @return
     */
    public Long getCetNumber(String username) throws Exception {
        CetNumber cetNumber = cetMapper.selectNumber(StringEncryptUtils.encryptString(username));
        if (cetNumber == null || cetNumber.getNumber() == null) {
            return null;
        }
        return cetNumber.getNumber();
    }

    /**
     * 保存四六级准考证号
     *
     * @param username
     * @param number
     * @return
     */
    public void saveCetNumber(String username, Long number) throws Exception {
        CetNumber cetNumber = cetMapper.selectNumber(StringEncryptUtils.encryptString(username));
        if (cetNumber == null) {
            cetMapper.insertNumber(StringEncryptUtils.encryptString(username), number);
        } else {
            cetMapper.updateNumber(StringEncryptUtils.encryptString(username), number);
        }
    }

}
